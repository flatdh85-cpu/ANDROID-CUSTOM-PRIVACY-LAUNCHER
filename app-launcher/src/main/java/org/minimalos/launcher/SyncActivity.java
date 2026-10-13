package org.minimalos.launcher;

import android.annotation.SuppressLint;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import org.minimalos.replication.SyncResult;

@SuppressLint({"SetTextI18n", "MissingPermission"}) // L'accès radio est vérifié avant la découverte.
public final class SyncActivity extends Activity {
    private final List<String> peerAddresses = new ArrayList<>();
    private Spinner transport;
    private Spinner peers;
    private Spinner interval;
    private EditText code;
    private TextView status;
    private TextView receiverStatus;
    private static final int[] INTERVALS = {10, 15, 20, 30};

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("Copie directe entre téléphones");
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 24));
        scroll.addView(root);
        root.addView(Ui.text(this, "Installez Minimal OS sur les deux téléphones. Choisissez Bluetooth (appareils déjà associés dans Android) ou Wi-Fi Direct (appareils à proximité). Aucun réseau Wi-Fi commun ni hotspot n’est requis. Entrez le même code sur les deux téléphones et démarrez leur réception.", 16, -1));
        transport = spinner(new String[]{"Bluetooth direct", "Wi-Fi Direct"});
        transport.setSelection(PeerSettings.WIFI_DIRECT.equals(PeerSettings.transport(this)) ? 1 : 0);
        root.addView(transport, Ui.matchWrap());
        transport.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int pos, long id) { showSavedPeer(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
        peers = spinner(new String[]{"Appuyez sur Chercher les appareils"});
        root.addView(peers, Ui.matchWrap());
        Button search = Ui.button(this, "Chercher les appareils à proximité");
        search.setOnClickListener(view -> discover());
        root.addView(search, Ui.matchWrap());
        Button pair = Ui.button(this, "Ouvrir l’association Bluetooth Android");
        pair.setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)));
        root.addView(pair, Ui.matchWrap());
        code = new EditText(this);
        code.setSingleLine(true);
        code.setHint("Même code de 32 caractères sur A et B");
        code.setText(PeerSettings.code(this));
        root.addView(code, Ui.matchWrap());
        root.addView(Ui.text(this, "Recopiez ce code sur l’autre téléphone avant la première copie. Ne le communiquez qu’au pair de confiance.", 14, -1));
        interval = spinner(new String[]{"10 minutes", "15 minutes", "20 minutes", "30 minutes"});
        int selected = PeerSettings.minutes(this);
        for (int i = 0; i < INTERVALS.length; i++) if (INTERVALS[i] == selected) interval.setSelection(i);
        root.addView(interval, Ui.matchWrap());
        root.addView(Ui.text(this, "Planifiez sur un seul téléphone : chaque session copie dans les deux sens. L’autre reste en réception. Android peut retarder la tentative ou limiter la découverte en arrière-plan.", 14, -1));
        Button save = Ui.button(this, "Enregistrer et planifier");
        save.setOnClickListener(view -> {
            if (!permissionsGranted() || !storageGranted() || !saveSettings()) return;
            SyncJobService.schedule(this);
            status.setText("Configuration enregistrée ; prochaine tentative différée.");
        });
        root.addView(save, Ui.matchWrap());
        Button listen = Ui.button(this, "Démarrer la réception directe");
        listen.setOnClickListener(view -> {
            if (!permissionsGranted() || !storageGranted() || !saveSettings()) return;
            stopService(new Intent(this, ReceiverService.class));
            try {
                Intent intent = new Intent(this, ReceiverService.class);
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent); else startService(intent);
                PeerSettings.prefs(this).edit().putBoolean("receiver_enabled", true).apply();
                status.setText("Réception " + selectedTransport() + " lancée. Gardez ce service actif sur le pair.");
            } catch (RuntimeException blocked) { status.setText("Réception refusée : " + blocked.getMessage()); }
        });
        root.addView(listen, Ui.matchWrap());
        Button stop = Ui.button(this, "Arrêter la réception et la planification");
        stop.setOnClickListener(view -> {
            PeerSettings.prefs(this).edit().putBoolean("receiver_enabled", false).apply();
            stopService(new Intent(this, ReceiverService.class));
            SyncJobService.cancel(this);
            status.setText("Réception et planification arrêtées.");
        });
        root.addView(stop, Ui.matchWrap());
        Button now = Ui.button(this, "Copier maintenant A ↔ B");
        now.setOnClickListener(view -> {
            if (!permissionsGranted() || !storageGranted() || !saveSettings()) return;
            status.setText("Connexion directe et copie en cours…");
            now.setEnabled(false);
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            new Thread(() -> {
                try {
                    SyncResult result = SyncWorker.sync(this);
                    runOnUiThread(() -> status.setText("Copie vérifiée : " + result.sent()
                            + " envoyés, " + result.received() + " reçus."));
                } catch (Exception failure) {
                    runOnUiThread(() -> status.setText("Copie échouée : " + failure.getMessage()));
                } finally { runOnUiThread(() -> {
                    now.setEnabled(true);
                    getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }); }
            }, "minimal-os-manual-sync").start();
        });
        root.addView(now, Ui.matchWrap());
        Button received = Ui.button(this, "Voir les copies reçues");
        received.setOnClickListener(view -> startActivity(new Intent(this, ReceivedMediaActivity.class)));
        root.addView(received, Ui.matchWrap());
        long last = PeerSettings.prefs(this).getLong("last_success_ms", 0);
        String lastText = last == 0 ? "Aucune copie vérifiée." : "Dernière copie vérifiée : "
                + java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(last))
                + " · " + PeerSettings.prefs(this).getInt("last_sent", 0) + " envoyés, "
                + PeerSettings.prefs(this).getInt("last_received", 0) + " reçus.";
        String error = PeerSettings.prefs(this).getString("last_error", null);
        status = Ui.text(this, error == null ? lastText : lastText + "\nDernier échec : " + error, 15, -1);
        root.addView(status, Ui.matchWrap());
        receiverStatus = Ui.text(this, "", 14, -1);
        root.addView(receiverStatus, Ui.matchWrap());
        Button refresh = Ui.button(this, "Actualiser l’état du récepteur");
        refresh.setOnClickListener(view -> refreshReceiverStatus());
        root.addView(refresh, Ui.matchWrap());
        setContentView(scroll);
        showSavedPeer();
        refreshReceiverStatus();
    }
    @Override protected void onResume() {
        super.onResume();
        refreshReceiverStatus();
    }
    private void refreshReceiverStatus() {
        if (receiverStatus == null) return;
        String error = PeerSettings.prefs(this).getString("last_receiver_error", null);
        receiverStatus.setText(error == null ? "" : "État du récepteur : " + error);
    }
    private Spinner spinner(String[] items) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return spinner;
    }
    private String selectedTransport() { return transport.getSelectedItemPosition() == 0 ? PeerSettings.BLUETOOTH : PeerSettings.WIFI_DIRECT; }
    private void showSavedPeer() {
        if (peers == null) return;
        peerAddresses.clear();
        String saved = selectedTransport().equals(PeerSettings.transport(this)) ? PeerSettings.peer(this) : "";
        if (saved.isEmpty()) peers.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Appuyez sur Chercher les appareils"}));
        else {
            peerAddresses.add(saved);
            peers.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    new String[]{"Appareil enregistré · " + saved}));
        }
    }
    private void showPeers(List<String> names, List<String> addresses) {
        peerAddresses.clear();
        peerAddresses.addAll(addresses);
        if (names.isEmpty()) names.add("Aucun appareil trouvé");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        peers.setAdapter(adapter);
        String saved = PeerSettings.peer(this);
        for (int i = 0; i < addresses.size(); i++) if (saved.equalsIgnoreCase(addresses.get(i))) peers.setSelection(i);
    }
    private void discover() {
        if (!permissionsGranted()) return;
        if (PeerSettings.BLUETOOTH.equals(selectedTransport())) {
            try {
                List<String> names = new ArrayList<>(), addresses = new ArrayList<>();
                for (android.bluetooth.BluetoothDevice device : BluetoothLink.bonded()) {
                    names.add(device.getName() + " · " + device.getAddress());
                    addresses.add(device.getAddress());
                }
                showPeers(names, addresses);
                status.setText(addresses.isEmpty() ? "Associez d’abord les deux téléphones dans les réglages Bluetooth Android." : "Choisissez le téléphone pair.");
            } catch (Exception failure) { status.setText("Bluetooth indisponible : " + failure.getMessage()); }
        } else {
            status.setText("Recherche Wi-Fi Direct en cours…");
            try {
                WifiDirectLink link = WifiDirectLink.create(this);
                link.info(info -> {
                    if (info != null && info.groupFormed
                            && PeerSettings.WIFI_DIRECT.equals(PeerSettings.transport(this))
                            && !PeerSettings.peer(this).isEmpty()) {
                        showSavedPeer();
                        status.setText("Groupe Wi-Fi Direct déjà actif. Essayez la copie avec le pair enregistré.");
                    } else startWifiDiscovery(link, false);
                });
            } catch (Exception failure) { status.setText("Wi-Fi Direct indisponible : " + failure.getMessage()); }
        }
    }
    private void startWifiDiscovery(WifiDirectLink link, boolean retried) {
        try { link.discover(new WifiP2pManager.ActionListener() {
            @Override public void onSuccess() { pollWifiPeers(link, 0, -1, retried); }
            @Override public void onFailure(int reason) {
                status.setText("Android a refusé la nouvelle recherche Wi-Fi Direct (code " + reason
                        + "). Lecture des pairs déjà connus…");
                pollWifiPeers(link, 0, reason, retried);
            }
        }); }
        catch (RuntimeException failure) { status.setText("Recherche Wi-Fi Direct indisponible : " + failure.getMessage()); }
    }
    private void pollWifiPeers(WifiDirectLink link, int attempt, int reason, boolean retried) {
        try { link.peers(list -> {
            List<String> names = new ArrayList<>(), addresses = new ArrayList<>();
            for (WifiP2pDevice device : list.getDeviceList()) {
                names.add(device.deviceName + " · " + device.deviceAddress);
                addresses.add(device.deviceAddress);
            }
            if (!addresses.isEmpty()) {
                showPeers(names, addresses);
                status.setText("Choisissez le pair Wi-Fi Direct ; la connexion sera établie lors de la copie.");
            } else if (attempt < 3) {
                new android.os.Handler(getMainLooper()).postDelayed(
                        () -> pollWifiPeers(link, attempt + 1, reason, retried), 3000);
            } else if (!retried) {
                status.setText("Aucun pair détecté ; nouvelle tentative Wi-Fi Direct…");
                startWifiDiscovery(link, true);
            } else {
                link.info(info -> {
                    if (info != null && info.groupFormed && !PeerSettings.peer(this).isEmpty()) {
                        showSavedPeer();
                        status.setText("Groupe Wi-Fi Direct déjà actif. Essayez la copie avec le pair enregistré.");
                    } else status.setText("Aucun pair Wi-Fi Direct visible. "
                            + (reason < 0 ? "La recherche n’a signalé aucune erreur précise. "
                                    : "Code Android " + reason + " : erreur de la pile Wi-Fi P2P. ")
                            + "Ouvrez les réglages Wi-Fi Direct des deux téléphones puis relancez la recherche.");
                });
            }
        }); }
        catch (RuntimeException failure) { status.setText("Lecture des pairs Wi-Fi Direct impossible : " + failure.getMessage()); }
    }
    private boolean saveSettings() {
        try {
            int chosen = peers.getSelectedItemPosition();
            if (chosen < 0 || chosen >= peerAddresses.size()) throw new IllegalArgumentException("Choisissez un appareil pair");
            PeerSettings.save(this, selectedTransport(), peerAddresses.get(chosen),
                    code.getText().toString().trim(), INTERVALS[interval.getSelectedItemPosition()]);
            return true;
        } catch (Exception invalid) { status.setText("Configuration invalide : " + invalid.getMessage()); return false; }
    }
    private boolean permissionsGranted() {
        if (Build.VERSION.SDK_INT < 23) return true;
        String permission = PeerSettings.BLUETOOTH.equals(selectedTransport())
                ? (Build.VERSION.SDK_INT >= 31 ? Manifest.permission.BLUETOOTH_CONNECT : null)
                : (Build.VERSION.SDK_INT >= 33 ? Manifest.permission.NEARBY_WIFI_DEVICES : Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != null && checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission) && Build.VERSION.SDK_INT >= 31)
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 47);
            else requestPermissions(new String[]{permission}, 47);
            status.setText("Accordez l’accès radio, puis relancez l’action.");
            return false;
        }
        return true;
    }
    private boolean storageGranted() {
        if (Build.VERSION.SDK_INT < 23) return true;
        String[] wanted = Build.VERSION.SDK_INT >= 34
                ? new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED}
                : Build.VERSION.SDK_INT >= 33
                ? new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}
                : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        int fullCount = Build.VERSION.SDK_INT >= 33 ? 2 : 1;
        for (int i = 0; i < fullCount; i++) if (checkSelfPermission(wanted[i]) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(wanted, 44);
            status.setText("Accordez l’accès complet aux photos et vidéos, puis relancez l’action.");
            return false;
        }
        return true;
    }
}
