package org.minimalos.launcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import java.io.IOException;
import java.net.Socket;
import org.minimalos.replication.LanSync;
import org.minimalos.replication.TcpReceiver;

/** The selected transport is reconciled on every start, including while already running. */
public final class ReceiverService extends Service {
    private Handler handler;
    private Reception reception;

    static void start(Context context) {
        Intent intent = new Intent(context, ReceiverService.class);
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent);
        else context.startService(intent);
        PeerSettings.prefs(context).edit().putBoolean("receiver_enabled", true).apply();
    }

    @Override public void onCreate() {
        super.onCreate();
        handler = new Handler(getMainLooper());
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26)
            manager.createNotificationChannel(new NotificationChannel("receiver", "Réception Minimal OS", NotificationManager.IMPORTANCE_LOW));
        PendingIntent pending = PendingIntent.getActivity(this, 0, new Intent(this, SyncActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, "receiver") : new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher).setContentTitle("Minimal OS : réception directe")
                .setContentText("État de la réception dans Copie entre téléphones").setOngoing(true).setContentIntent(pending);
        startForeground(42, builder.build());
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        String selected = PeerSettings.transport(this);
        if (reception == null || !reception.active || !selected.equals(reception.transport)) {
            if (reception != null) reception.close();
            reception = new Reception(selected);
            reception.start();
        } else if (reception.p2p != null) reception.refreshWifiGroup();
        return START_STICKY;
    }

    private void showState(String state, String error) {
        android.content.SharedPreferences.Editor edit = PeerSettings.prefs(this).edit()
                .putString("receiver_status", state);
        if (error == null) edit.remove("last_receiver_error");
        else edit.putString("last_receiver_error", error);
        edit.apply();
    }

    /** Resources belong to one transport generation; an old worker cannot stop its replacement. */
    private final class Reception {
        final String transport;
        volatile boolean active = true;
        private BluetoothServerSocket bluetoothServer;
        private BluetoothSocket bluetoothClient;
        private TcpReceiver wifiServer;
        private String wifiAddress;
        private WifiDirectLink p2p;
        private BroadcastReceiver p2pReceiver;
        private boolean p2pRegistered;
        private long wifiQuery;
        private final Runnable pollWifi = new Runnable() {
            @Override public void run() {
                if (!active) return;
                refreshWifiGroup();
                handler.postDelayed(this, 3000);
            }
        };

        Reception(String transport) { this.transport = transport; }
        void start() {
            showState("Démarrage du récepteur " + transport + "…", null);
            if (PeerSettings.BLUETOOTH.equals(transport))
                new Thread(this::listenBluetooth, "minimal-os-bluetooth-receiver").start();
            else startWifiDirect();
        }
        void state(String state, String error) {
            handler.post(() -> { if (reception == this && active) showState(state, error); });
        }
        void wifiState(TcpReceiver source, String state, String error) {
            handler.post(() -> {
                if (reception == this && active && wifiServer == source) showState(state, error);
            });
        }
        void failed(String message, Exception failure) {
            android.util.Log.w("MinimalOS", message, failure);
            handler.post(() -> {
                if (reception != this || !active) return;
                showState("Récepteur arrêté", message + ": " + failure.getMessage());
                close();
            });
        }

        @android.annotation.SuppressLint("MissingPermission")
        private void listenBluetooth() {
            try (BluetoothServerSocket listener = BluetoothLink.listen()) {
                synchronized (this) {
                    if (!active) return;
                    bluetoothServer = listener;
                }
                state("Bluetooth : en écoute", null);
                while (active) {
                    // Accept errors are terminal; session errors leave the listener available.
                    try (BluetoothSocket socket = listener.accept()) {
                        synchronized (this) {
                            if (!active) return;
                            bluetoothClient = socket;
                        }
                        try {
                            state("Bluetooth : authentification / échange en cours", null);
                            LanSync.server(socket.getInputStream(), socket.getOutputStream(),
                                    LanSync.parseKey(PeerSettings.code(ReceiverService.this)), new GalleryRepository(ReceiverService.this));
                            state("Bluetooth : session terminée, en écoute", null);
                        } catch (Exception failure) {
                            if (active) state("Bluetooth : en écoute après échec", failure.getMessage());
                            android.util.Log.w("MinimalOS", "Session Bluetooth échouée", failure);
                        } finally { synchronized (this) { bluetoothClient = null; } }
                    }
                }
            } catch (Exception failure) { if (active) failed("Récepteur Bluetooth indisponible", failure); }
        }

        private void startWifiDirect() {
            try {
                p2p = WifiDirectLink.create(ReceiverService.this);
                p2pReceiver = new BroadcastReceiver() {
                    @Override public void onReceive(Context context, Intent intent) { refreshWifiGroup(); }
                };
                registerReceiver(p2pReceiver, new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION));
                p2pRegistered = true;
                handler.post(pollWifi);
                discoverWifi(0);
            } catch (Exception failure) { failed("Initialisation Wi-Fi Direct impossible", failure); }
        }
        private void discoverWifi(int attempt) {
            if (!active) return;
            try { p2p.discover(new WifiP2pManager.ActionListener() {
                @Override public void onSuccess() { }
                @Override public void onFailure(int reason) {
                    if (active && attempt < 2) handler.postDelayed(() -> discoverWifi(attempt + 1), 5000);
                }
            }); } catch (RuntimeException failure) { state("Wi-Fi Direct : découverte indisponible", failure.getMessage()); }
        }
        private void refreshWifiGroup() {
            if (!active) return;
            long query = ++wifiQuery;
            try { p2p.info(info -> { if (query == wifiQuery) onWifiInfo(info); }); }
            catch (RuntimeException failure) { state("Wi-Fi Direct : état indisponible", failure.getMessage()); }
        }
        private void onWifiInfo(WifiP2pInfo info) {
            if (!active) return;
            if (info == null) return; // An unanswered state query does not establish a disconnection.
            if (!info.groupFormed || info.groupOwnerAddress == null) {
                closeWifi();
                state("Wi-Fi Direct : en attente du groupe", null);
                return;
            }
            if (!info.isGroupOwner) {
                closeWifi();
                state("Wi-Fi Direct : groupe connecté ; lancez la copie depuis ce téléphone", null);
                return;
            }
            String address = info.groupOwnerAddress.getHostAddress();
            if (wifiServer != null && !wifiServer.isClosed() && address.equals(wifiAddress)) return;
            closeWifi();
            TcpReceiver listener = new TcpReceiver(info.groupOwnerAddress, LanSync.PORT);
            wifiServer = listener;
            wifiAddress = address;
            state("Wi-Fi Direct : ouverture de la réception sur " + address, null);
            new Thread(() -> {
                try {
                    listener.serve(new TcpReceiver.Listener() {
                        @Override public void listening(int port) {
                            wifiState(listener, "Wi-Fi Direct : en écoute sur " + address + ":" + port
                                    + " ; lancez la copie depuis l'autre téléphone", null);
                        }
                        @Override public void session(Socket socket) throws IOException {
                            wifiState(listener, "Wi-Fi Direct : authentification / échange en cours", null);
                            LanSync.server(socket, LanSync.parseKey(PeerSettings.code(ReceiverService.this)),
                                    new GalleryRepository(ReceiverService.this));
                            wifiState(listener, "Wi-Fi Direct : session terminée, en écoute sur " + address, null);
                        }
                        @Override public void sessionFailed(IOException failure) {
                            wifiState(listener, "Wi-Fi Direct : en écoute après échec", failure.getMessage());
                        }
                    });
                } catch (Exception failure) {
                    wifiState(listener, "Wi-Fi Direct : ouverture à réessayer", failure.getMessage());
                    android.util.Log.w("MinimalOS", "Écoute Wi-Fi Direct indisponible", failure);
                }
                // Polling retries binding when Android finishes configuring the interface.
            }, "minimal-os-wifi-direct-receiver").start();
        }
        private void closeWifi() {
            if (wifiServer != null) wifiServer.close();
            wifiServer = null;
            wifiAddress = null;
        }
        synchronized void close() {
            active = false;
            handler.removeCallbacks(pollWifi);
            if (p2pRegistered) { unregisterReceiver(p2pReceiver); p2pRegistered = false; }
            if (p2p != null) p2p.close();
            try { if (bluetoothClient != null) bluetoothClient.close(); } catch (IOException ignored) { }
            try { if (bluetoothServer != null) bluetoothServer.close(); } catch (IOException ignored) { }
            closeWifi();
        }
    }

    @Override public void onDestroy() {
        Reception old = reception;
        reception = null;
        if (old != null) old.close();
        handler.removeCallbacksAndMessages(null);
        showState("Réception arrêtée", PeerSettings.prefs(this).getString("last_receiver_error", null));
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
