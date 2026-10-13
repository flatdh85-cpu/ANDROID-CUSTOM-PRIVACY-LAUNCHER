package org.minimalos.launcher;

import android.annotation.SuppressLint;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.Collections;
import org.minimalos.replication.SyncResult;

@SuppressLint("SetTextI18n") // Interface française du prototype ; les messages décrivent l'état direct des transferts.
public final class SyncActivity extends Activity {
    private EditText address;
    private EditText code;
    private Spinner interval;
    private TextView status;
    private static final int[] INTERVALS = {10, 15, 20, 30};

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("Copie entre téléphones");
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 24));
        scroll.addView(root);
        root.addView(Ui.text(this, "Installez cet APK sur les deux téléphones. Reliez-les au même Wi-Fi ou au hotspot de l’un d’eux. Sur A, lancez la réception et recopiez son adresse et son code sur B. La copie fonctionne dans les deux sens.", 16, -1));
        root.addView(Ui.text(this, "Adresse de ce téléphone : " + localAddress(), 15, -1));
        address = new EditText(this);
        address.setSingleLine(true);
        address.setHint("Adresse IPv4 du pair");
        address.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        address.setText(PeerSettings.address(this));
        root.addView(address, Ui.matchWrap());
        code = new EditText(this);
        code.setSingleLine(true);
        code.setHint("Code partagé (32 caractères)");
        code.setText(PeerSettings.code(this));
        root.addView(code, Ui.matchWrap());
        interval = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"10 minutes", "15 minutes", "20 minutes", "30 minutes"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        interval.setAdapter(adapter);
        int selected = PeerSettings.minutes(this);
        for (int i = 0; i < INTERVALS.length; i++) if (INTERVALS[i] == selected) interval.setSelection(i);
        root.addView(interval, Ui.matchWrap());
        root.addView(Ui.text(this, "Android peut retarder une tentative ; le récepteur doit rester actif sur le pair. Les fichiers reçus sont conservés dans l’espace privé de Minimal OS.", 14, -1));
        Button save = Ui.button(this, "Enregistrer et planifier");
        save.setOnClickListener(view -> {
            if (!storageGranted()) return;
            if (!saveSettings()) return;
            SyncJobService.schedule(this);
            status.setText("Configuration enregistrée ; prochaine tentative différée.");
        });
        root.addView(save, Ui.matchWrap());
        Button listen = Ui.button(this, "Démarrer la réception");
        listen.setOnClickListener(view -> {
            if (!storageGranted()) return;
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 46);
                status.setText("Accordez la notification du récepteur, puis relancez la réception.");
                return;
            }
            try { PeerSettings.saveCode(this, code.getText().toString().trim()); }
            catch (Exception invalid) { status.setText("Code invalide : " + invalid.getMessage()); return; }
            Intent intent = new Intent(this, ReceiverService.class);
            try {
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent); else startService(intent);
                PeerSettings.prefs(this).edit().putBoolean("receiver_enabled", true).apply();
                status.setText("Récepteur lancé sur le port 38742. Gardez sa notification active.");
            } catch (RuntimeException blocked) { status.setText("Réception refusée par Android : " + blocked.getMessage()); }
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
            if (!storageGranted()) return;
            if (!saveSettings()) return;
            status.setText("Copie en cours ; gardez les deux téléphones connectés…");
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
        setContentView(scroll);
    }

    private boolean saveSettings() {
        try {
            PeerSettings.save(this, address.getText().toString().trim(),
                    code.getText().toString().trim(), INTERVALS[interval.getSelectedItemPosition()]);
            return true;
        } catch (Exception invalid) {
            status.setText("Configuration invalide : " + invalid.getMessage());
            return false;
        }
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

    static String localAddress() {
        try {
            StringBuilder addresses = new StringBuilder();
            for (NetworkInterface network : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (java.net.InetAddress address : Collections.list(network.getInetAddresses())) {
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()
                            && address.isSiteLocalAddress()) {
                        if (addresses.length() > 0) addresses.append(", ");
                        addresses.append(address.getHostAddress());
                    }
                }
            }
            if (addresses.length() > 0) return addresses.toString();
        } catch (Exception ignored) { }
        return "indisponible";
    }
}
