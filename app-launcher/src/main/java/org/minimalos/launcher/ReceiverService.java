package org.minimalos.launcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.IBinder;
import java.net.ServerSocket;
import java.net.Socket;
import org.minimalos.replication.LanSync;

/** Foreground receiver for direct Bluetooth or Wi-Fi P2P radio links. */
public final class ReceiverService extends Service {
    private volatile boolean running;
    private BluetoothServerSocket bluetoothServer;
    private ServerSocket wifiServer;
    private BroadcastReceiver p2pReceiver;
    private WifiDirectLink p2p;

    private void receiverError(String message) {
        PeerSettings.prefs(this).edit().putString("last_receiver_error", message).apply();
    }
    private void receiverReady() {
        PeerSettings.prefs(this).edit().remove("last_receiver_error").apply();
    }

    @Override public void onCreate() {
        super.onCreate();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26)
            manager.createNotificationChannel(new NotificationChannel("receiver", "Réception Minimal OS", NotificationManager.IMPORTANCE_LOW));
        PendingIntent pending = PendingIntent.getActivity(this, 0, new Intent(this, SyncActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, "receiver") : new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher).setContentTitle("Minimal OS : réception directe")
                .setContentText("Copie depuis un appareil appairé").setOngoing(true).setContentIntent(pending);
        startForeground(42, builder.build());
        running = true;
        if (PeerSettings.BLUETOOTH.equals(PeerSettings.transport(this)))
            new Thread(this::listenBluetooth, "minimal-os-bluetooth-receiver").start();
        else startWifiDirect();
    }

    @android.annotation.SuppressLint("MissingPermission") // BLUETOOTH_CONNECT demandé avant démarrage ; échec capturé si retiré.
    private void listenBluetooth() {
        try (BluetoothServerSocket listener = BluetoothLink.listen()) {
            bluetoothServer = listener;
            receiverReady();
            while (running) {
                try (BluetoothSocket socket = listener.accept()) {
                    String remoteAddress = socket.getRemoteDevice().getAddress();
                    // The shared code authenticates the peer; the saved radio address is for outgoing calls.
                    LanSync.server(socket.getInputStream(), socket.getOutputStream(),
                            LanSync.parseKey(PeerSettings.code(this)), new GalleryRepository(this));
                    PeerSettings.prefs(this).edit().putString("last_receiver_peer", remoteAddress).apply();
                    if (PeerSettings.peer(this).equalsIgnoreCase(remoteAddress)) receiverReady();
                    else receiverError("Copie reçue du pair authentifié " + remoteAddress
                            + ". Sélectionnez cette adresse ici pour les copies sortantes.");
                } catch (Exception failed) {
                    if (running) {
                        receiverError(failed.getMessage() == null ? failed.getClass().getSimpleName() : failed.getMessage());
                        android.util.Log.w("MinimalOS", "Session Bluetooth échouée", failed);
                    }
                }
            }
        } catch (Exception unavailable) {
            if (running) {
                receiverError("Récepteur Bluetooth indisponible : " + unavailable.getMessage());
                android.util.Log.e("MinimalOS", "Récepteur Bluetooth indisponible", unavailable);
            }
        }
        stopSelf();
    }

    private void startWifiDirect() {
        try { p2p = WifiDirectLink.create(this); }
        catch (Exception unavailable) { android.util.Log.e("MinimalOS", "Wi-Fi Direct indisponible", unavailable); stopSelf(); return; }
        p2pReceiver = new BroadcastReceiver() {
            @Override public void onReceive(android.content.Context context, Intent intent) { refreshWifiGroup(); }
        };
        registerReceiver(p2pReceiver, new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION));
        refreshWifiGroup();
        try { p2p.discover(new WifiP2pManager.ActionListener() {
            @Override public void onSuccess() { }
            @Override public void onFailure(int reason) {
                android.util.Log.w("MinimalOS", "Découverte Wi-Fi Direct refusée : " + reason);
            }
        }); } catch (RuntimeException failure) {
            android.util.Log.w("MinimalOS", "Découverte Wi-Fi Direct indisponible", failure);
        }
    }
    private void refreshWifiGroup() {
        try { p2p.info(this::onWifiInfo); }
        catch (RuntimeException failure) { android.util.Log.w("MinimalOS", "État Wi-Fi Direct indisponible", failure); }
    }
    private void onWifiInfo(WifiP2pInfo info) {
        if (info == null || !info.groupFormed || !info.isGroupOwner || info.groupOwnerAddress == null) {
            try { if (wifiServer != null) wifiServer.close(); } catch (Exception ignored) { }
            wifiServer = null;
            return;
        }
        if (wifiServer != null && !wifiServer.isClosed()) return;
        new Thread(() -> listenWifi(info), "minimal-os-wifi-direct-receiver").start();
    }
    private void listenWifi(WifiP2pInfo info) {
        try (ServerSocket listener = new ServerSocket(LanSync.PORT, 2, info.groupOwnerAddress)) {
            wifiServer = listener;
            receiverReady();
            while (running) {
                try (Socket socket = listener.accept()) {
                    LanSync.server(socket, LanSync.parseKey(PeerSettings.code(this)), new GalleryRepository(this));
                    receiverReady();
                } catch (Exception failed) {
                    if (running) {
                        receiverError(failed.getMessage() == null ? failed.getClass().getSimpleName() : failed.getMessage());
                        android.util.Log.w("MinimalOS", "Session Wi-Fi Direct échouée", failed);
                    }
                }
            }
        } catch (Exception unavailable) {
            if (running) {
                receiverError("Récepteur Wi-Fi Direct indisponible : " + unavailable.getMessage());
                android.util.Log.e("MinimalOS", "Récepteur Wi-Fi Direct indisponible", unavailable);
            }
        }
    }
    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override public void onDestroy() {
        running = false;
        if (p2pReceiver != null) unregisterReceiver(p2pReceiver);
        try { if (bluetoothServer != null) bluetoothServer.close(); } catch (Exception ignored) { }
        try { if (wifiServer != null) wifiServer.close(); } catch (Exception ignored) { }
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
