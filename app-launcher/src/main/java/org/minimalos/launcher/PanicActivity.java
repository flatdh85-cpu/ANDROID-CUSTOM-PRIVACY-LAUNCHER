package org.minimalos.launcher;

import android.annotation.SuppressLint;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.minimalos.replication.MediaRecord;
import org.minimalos.replication.SyncResult;

/** Guided Android confirmations. Never reports deletion or backup before verification. */
@SuppressLint("SetTextI18n") // Parcours français avec détails d'erreur et d'état produits à l'exécution.
public final class PanicActivity extends Activity {
    private static final int UNINSTALL = 101;
    private static final int DELETE_MEDIA = 102;
    private static final int LEGACY_DELETE = 103;
    private static final String[] COMMUNICATION = {"telegram", "simplex", "briar", "bridgefy", "bitchat", "localsend"};
    private static final String[] UTILITIES = {"shizuku", "canta"};
    private final List<String> uninstallQueue = new ArrayList<>();
    private final List<Uri> mediaQueue = new ArrayList<>();
    private Set<String> confirmedOnPeer;
    private int stage;
    private String currentPackage;
    private TextView status;
    private volatile boolean aborted;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("Panic");
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        status = Ui.text(this, "Panic demande des confirmations Android. Les données des messageries peuvent être perdues avant la copie de la galerie. La galerie ne sera proposée à l’effacement qu’après réception vérifiée sur le pair.", 17, -1);
        root.addView(status, Ui.matchWrap());
        Button start = Ui.button(this, "Commencer Panic");
        start.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Confirmer Panic")
                .setMessage("Les messageries seront proposées à la désinstallation en premier. Ensuite, la galerie sera copiée sur le pair configuré, puis Android demandera son effacement. Vérifiez que l’autre téléphone est allumé, connecté et en réception.")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Continuer", (dialog, which) -> {
                    if (PeerSettings.peer(this).isEmpty()) {
                        status.setText("Choisissez d’abord un pair direct et testez une copie manuelle.");
                        return;
                    }
                    if (!hasOtherHome()) {
                        status.setText("Panic bloqué : aucun autre launcher HOME actif détecté.");
                        return;
                    }
                    if (!storageGranted()) return;
                    start.setEnabled(false);
                    status.setText("Vérification de la connexion au pair…");
                    new Thread(() -> {
                        try {
                            SyncWorker.probe(this);
                            runOnUiThread(() -> {
                                if (aborted) return;
                                uninstallQueue.clear();
                                uninstallQueue.addAll(findApps(COMMUNICATION));
                                stage = 1;
                                nextUninstall();
                            });
                        } catch (Exception absent) {
                            runOnUiThread(() -> {
                                start.setEnabled(true);
                                status.setText("Panic arrêté avant toute désinstallation : pair inaccessible. " + absent.getMessage());
                            });
                        }
                    }, "minimal-os-panic-probe").start();
                }).show());
        root.addView(start, Ui.matchWrap());
        Button abort = Ui.button(this, "Quitter Panic");
        abort.setOnClickListener(view -> finish());
        root.addView(abort, Ui.matchWrap());
        setContentView(root);
    }

    private List<String> findApps(String[] tokens) {
        List<String> result = new ArrayList<>();
        PackageManager manager = getPackageManager();
        for (ApplicationInfo app : manager.getInstalledApplications(0)) {
            if (app.packageName.equals(getPackageName()) || (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
            String label = manager.getApplicationLabel(app).toString().toLowerCase(java.util.Locale.ROOT);
            for (String token : tokens) {
                boolean knownPackage = false;
                for (AppEntry entry : AppCatalog.entries()) {
                    if (entry.label().toLowerCase(java.util.Locale.ROOT).startsWith(token)
                            && entry.packageName().equals(app.packageName)) knownPackage = true;
                }
                if (knownPackage || label.equals(token) || label.startsWith(token + " ")) {
                    result.add(app.packageName);
                    break;
                }
            }
        }
        return result;
    }

    private void nextUninstall() {
        if (uninstallQueue.isEmpty()) {
            if (stage == 1) copyBeforeDelete();
            else if (stage == 3) offerOwnUninstall();
            return;
        }
        currentPackage = uninstallQueue.remove(0);
        status.setText("Confirmation Android pour " + currentPackage + "…");
        Intent delete = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + currentPackage));
        delete.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        try { startActivityForResult(delete, UNINSTALL); }
        catch (Exception unavailable) { status.setText("Désinstallation indisponible : " + currentPackage); }
    }

    private void copyBeforeDelete() {
        if (!storageGranted()) return;
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        status.setText("Copie et vérification de toute la galerie en cours…");
        new Thread(() -> {
            try {
                SyncResult result = SyncWorker.sync(this);
                GalleryRepository repository = new GalleryRepository(this);
                List<MediaRecord> current = repository.list();
                for (MediaRecord record : current) {
                    if (!result.confirmedOnPeer().contains(record.id()))
                        throw new IllegalStateException("Nouveau média non copié : " + record.id());
                }
                Map<Uri, String> gallery = repository.allGalleryUris();
                runOnUiThread(() -> {
                    if (aborted) return;
                    getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    confirmedOnPeer = result.confirmedOnPeer();
                    mediaQueue.clear();
                    mediaQueue.addAll(gallery.keySet());
                    status.setText("Copie vérifiée sur le pair. " + mediaQueue.size()
                            + " médias publics à supprimer avec Android.");
                    deleteNextBatch();
                });
            } catch (Exception failure) {
                runOnUiThread(() -> {
                    getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    status.setText("Panic arrêté : copie non vérifiée. " + failure.getMessage()
                            + "\nOuvrez Copie entre téléphones pour corriger la connexion.");
                });
            }
        }, "minimal-os-panic-sync").start();
    }

    private void deleteNextBatch() {
        if (aborted) return;
        if (confirmedOnPeer == null) { status.setText("Panic arrêté : reçu absent."); return; }
        if (mediaQueue.isEmpty()) { verifyGalleryGone(); return; }
        if (Build.VERSION.SDK_INT < 30) {
            deleteLegacy();
            return;
        }
        ArrayList<Uri> batch = new ArrayList<>();
        for (int i = 0; i < Math.min(100, mediaQueue.size()); i++) batch.add(mediaQueue.get(i));
        try {
            PendingIntent request = MediaStore.createDeleteRequest(getContentResolver(), batch);
            startIntentSenderForResult(request.getIntentSender(), DELETE_MEDIA, null, 0, 0, 0);
        } catch (Exception failure) { status.setText("Effacement interrompu : " + failure.getMessage()); }
    }

    private void deleteLegacy() {
        if (Build.VERSION.SDK_INT == 29) { deleteAndroidTen(); return; }
        new Thread(() -> {
            while (!aborted && !mediaQueue.isEmpty()) {
                Uri uri = mediaQueue.get(0);
                try {
                    if (getContentResolver().delete(uri, null, null) != 1)
                        throw new IllegalStateException("Média encore présent : " + uri);
                    mediaQueue.remove(0);
                } catch (Exception failure) {
                    runOnUiThread(() -> status.setText("Suppression interrompue : " + failure.getMessage()));
                    return;
                }
            }
            if (!aborted) runOnUiThread(this::verifyGalleryGone);
        }, "minimal-os-delete-legacy").start();
    }

    @android.annotation.TargetApi(29)
    private void deleteAndroidTen() {
        while (!mediaQueue.isEmpty()) {
            Uri uri = mediaQueue.get(0);
            try {
                if (getContentResolver().delete(uri, null, null) != 1)
                    throw new IllegalStateException("Média encore présent : " + uri);
                mediaQueue.remove(0);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(this::deleteNextBatch);
                return;
            } catch (android.app.RecoverableSecurityException needsApproval) {
                try {
                    startIntentSenderForResult(needsApproval.getUserAction().getActionIntent().getIntentSender(),
                            LEGACY_DELETE, null, 0, 0, 0);
                } catch (Exception failure) { status.setText("Suppression interrompue : " + failure.getMessage()); }
                return;
            } catch (Exception failure) { status.setText("Suppression interrompue : " + failure.getMessage()); return; }
        }
        verifyGalleryGone();
    }

    private void verifyGalleryGone() {
        new Thread(() -> {
            try {
                GalleryRepository repository = new GalleryRepository(this);
                List<MediaRecord> current = repository.list();
                if (!repository.allGalleryUris().isEmpty()) throw new IllegalStateException("Des médias restent dans la galerie");
                for (MediaRecord record : current) if (!confirmedOnPeer.contains(record.id()))
                    throw new IllegalStateException("Une copie privée n’est pas confirmée sur le pair");
                runOnUiThread(() -> {
                    stage = 3;
                    uninstallQueue.clear();
                    uninstallQueue.addAll(findApps(UTILITIES));
                    nextUninstall();
                });
            } catch (Exception failure) {
                runOnUiThread(() -> status.setText("Panic arrêté : " + failure.getMessage()));
            }
        }, "minimal-os-verify-delete").start();
    }

    private void offerOwnUninstall() {
        if (!hasOtherHome()) {
            status.setText("Panic arrêté : aucun autre launcher HOME actif détecté. Réactivez le launcher constructeur avant de désinstaller Minimal OS.");
            return;
        }
        status.setText("Galerie effacée. Les copies reçues localement seront perdues avec Minimal OS ; elles ont été confirmées sur le pair.");
        new AlertDialog.Builder(this).setTitle("Dernière étape")
                .setMessage("Android va demander de désinstaller Minimal OS. Le launcher natif doit être disponible pour reprendre l’écran d’accueil.")
                .setNegativeButton("Garder Minimal OS", null)
                .setPositiveButton("Désinstaller Minimal OS", (dialog, which) -> {
                    SyncJobService.cancel(this);
                    PeerSettings.prefs(this).edit().putBoolean("receiver_enabled", false).apply();
                    stopService(new Intent(this, ReceiverService.class));
                    Intent delete = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + getPackageName()));
                    startActivity(delete);
                }).show();
    }

    private boolean hasOtherHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        for (android.content.pm.ResolveInfo resolved : getPackageManager().queryIntentActivities(home, 0)) {
            if (resolved.activityInfo != null && !getPackageName().equals(resolved.activityInfo.packageName)
                    && resolved.activityInfo.enabled && resolved.activityInfo.applicationInfo.enabled) return true;
        }
        return false;
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == UNINSTALL) {
            try {
                getPackageManager().getPackageInfo(currentPackage, 0);
                status.setText("Panic arrêté : " + currentPackage + " est toujours installée.");
            } catch (PackageManager.NameNotFoundException removed) { nextUninstall(); }
        } else if (request == DELETE_MEDIA) {
            if (result != RESULT_OK) { status.setText("Panic arrêté : suppression de la galerie refusée."); return; }
            for (int i = 0; i < Math.min(100, mediaQueue.size()); i++) mediaQueue.remove(0);
            deleteNextBatch();
        } else if (request == LEGACY_DELETE) {
            if (result != RESULT_OK) status.setText("Panic arrêté : suppression refusée.");
            else deleteNextBatch();
        }
    }

    private boolean storageGranted() {
        if (Build.VERSION.SDK_INT < 23) return true;
        String[] wanted = Build.VERSION.SDK_INT >= 34
                ? new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED}
                : Build.VERSION.SDK_INT >= 33
                ? new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}
                : Build.VERSION.SDK_INT <= 29
                    ? new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        int fullCount = Build.VERSION.SDK_INT >= 33 ? 2 : wanted.length;
        for (int i = 0; i < fullCount; i++) if (checkSelfPermission(wanted[i]) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(wanted, 45);
            status.setText("Accordez l’accès complet aux médias, puis relancez Panic.");
            return false;
        }
        return true;
    }

    @Override protected void onDestroy() {
        aborted = true;
        super.onDestroy();
    }
}
