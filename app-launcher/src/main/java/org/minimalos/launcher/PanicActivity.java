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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;



/** Guided Android confirmations. Never reports deletion or backup before verification. */
@SuppressLint("SetTextI18n") // Parcours français avec détails d'erreur et d'état produits à l'exécution.
public final class PanicActivity extends Activity {
    private static final int UNINSTALL = 101;
    private static final int DELETE_MEDIA = 102;
    private static final int LEGACY_DELETE = 103;
    private final List<String> uninstallQueue = new ArrayList<>();
    private final List<Uri> mediaQueue = new ArrayList<>();
    private int batchCount;
    private Button start;
    private String currentPackage;
    private TextView status;
    private volatile boolean aborted;
    private boolean removingSelf;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("Panic");
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        status = Ui.text(this, "Panic supprime LocalSend, BitChat et SimpleX si elles sont installées, puis les photos et vidéos de la galerie et enfin Minimal OS. Aucune copie, aucun transfert, aucune attente d’un autre téléphone. Android demande ses confirmations. Les autres applications et le launcher constructeur sont conservés.", 17, -1);
        root.addView(status, Ui.matchWrap());
        start = Ui.button(this, "Commencer Panic");
        start.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Confirmer Panic")
                .setMessage("Supprimer les applications suivantes et leurs données locales :\n" + android.text.TextUtils.join("\n", findApps())
                        + "\n\nPuis effacer TOUTES les photos et vidéos accessibles de la galerie, y compris sur la carte SD, et désinstaller Minimal OS avec ses anciennes copies privées. Aucune sauvegarde ne sera lancée. Les copies distantes ne sont pas effacées.")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    begin();
                }).show());
        root.addView(start, Ui.matchWrap());
        Button automatic = Ui.button(this, "Configurer l’auto-validation Panic");
        automatic.setOnClickListener(view -> new AlertDialog.Builder(this).setTitle("Autoriser Panic automatique ?")
                .setMessage("Après activation du service d’accessibilité Android, chaque appui long sur Panic lancera immédiatement la suppression de LocalSend, BitChat, SimpleX, de toute la galerie accessible puis de Minimal OS, sans sauvegarde. Le service tentera de valider leurs dialogues système. Les autres applications sont conservées.\n\nLe service peut lire les fenêtres nécessaires aux confirmations ; aucun contenu n’est enregistré ou transmis. Les fenêtres peuvent apparaître brièvement. Un dialogue inconnu restera à valider manuellement. Cette autorisation Android doit être accordée par vous.")
                .setNegativeButton("Annuler", null).setPositiveButton("Autoriser et ouvrir Android", (dialog, which) -> {
                    getSharedPreferences("panic", MODE_PRIVATE).edit().putBoolean("automatic", true).apply();
                    startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }).show());
        root.addView(automatic, Ui.matchWrap());
        Button disable = Ui.button(this, "Désactiver l’auto-validation");
        disable.setOnClickListener(view -> {
            getSharedPreferences("panic", MODE_PRIVATE).edit().putBoolean("automatic", false).apply();
            PanicAutomation.clear(); status.setText("Auto-validation désactivée. Android demandera ses confirmations.");
        });
        root.addView(disable, Ui.matchWrap());
        Button abort = Ui.button(this, "Quitter Panic");
        abort.setOnClickListener(view -> { aborted = true; PanicAutomation.clear(); finish(); });
        root.addView(abort, Ui.matchWrap());
        android.widget.ScrollView scroll = new android.widget.ScrollView(this); scroll.addView(root);
        setContentView(scroll);
        if (state != null) {
            uninstallQueue.addAll(state.getStringArrayList("apps") == null ? new ArrayList<>() : state.getStringArrayList("apps"));
            currentPackage = state.getString("current"); batchCount = state.getInt("batch");
            start.setEnabled(true); status.setText(state.getString("status", "Procédure interrompue : relancez Panic."));
        }
        if (state == null && getIntent().getBooleanExtra("execute", false) && PanicAutomation.enabled(this)) begin();
    }

    private void begin() {
        if (!hasOtherHome()) { status.setText("Panic bloqué : aucun autre launcher HOME actif détecté."); return; }
        if (!storageGranted()) return;
        start.setEnabled(false);
        BootReceiver.retireTransfers(this);
        uninstallQueue.clear(); uninstallQueue.addAll(findApps()); nextUninstall();
    }

    private List<String> findApps() {
        List<String> result = new ArrayList<>();
        for (AppEntry entry : AppCatalog.entries()) {
            String name = AppCatalog.resolvePackageName(this, entry);
            try {
                ApplicationInfo app = getPackageManager().getApplicationInfo(name, 0);
                if (!name.equals(getPackageName()) && (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) result.add(name);
            } catch (PackageManager.NameNotFoundException absent) { }
        }
        return result;
    }

    private void nextUninstall() {
        if (uninstallQueue.isEmpty()) {
            prepareGalleryDelete();
            return;
        }
        currentPackage = uninstallQueue.remove(0);
        status.setText("Confirmation Android pour " + currentPackage + "…");
        Intent delete = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + currentPackage));
        delete.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        PanicAutomation.uninstall(this, delete, currentPackage);
        try { startActivityForResult(delete, UNINSTALL); }
        catch (Exception unavailable) { PanicAutomation.clear(); status.setText("Désinstallation indisponible : " + currentPackage); }
    }

    private void prepareGalleryDelete() {
        status.setText("Lecture de la liste des photos et vidéos à supprimer…");
        new Thread(() -> {
            try {
                List<Uri> gallery = GalleryAccess.galleryUris(this);
                runOnUiThread(() -> {
                    if (aborted) return;
                    mediaQueue.clear(); mediaQueue.addAll(gallery); deleteNextBatch();
                });
            } catch (Exception error) { runOnUiThread(() -> status.setText("Effacement interrompu : " + error.getMessage())); }
        }, "panic-gallery-list").start();
    }

    private void deleteNextBatch() {
        if (aborted) return;
        if (mediaQueue.isEmpty()) { verifyGalleryGone(); return; }
        if (Build.VERSION.SDK_INT < 30) {
            deleteLegacy();
            return;
        }
        batchCount = Math.min(100, mediaQueue.size());
        ArrayList<Uri> batch = new ArrayList<>(mediaQueue.subList(0, batchCount));
        status.setText("Confirmation Android : " + batchCount + " médias (" + mediaQueue.size() + " restants)…");
        try {
            PendingIntent request = MediaStore.createDeleteRequest(getContentResolver(), batch);
            PanicAutomation.expect(this, "media-delete", request.getCreatorPackage(), getString(R.string.app_name));
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
                    PendingIntent approval = needsApproval.getUserAction().getActionIntent();
                    PanicAutomation.expect(this, "media-grant", approval.getCreatorPackage(), getString(R.string.app_name));
                    startIntentSenderForResult(approval.getIntentSender(),
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
                if (!GalleryAccess.galleryUris(this).isEmpty()) throw new IllegalStateException("Des médias restent dans la galerie. Relancez Panic pour les supprimer.");
                runOnUiThread(() -> { if (!aborted) offerOwnUninstall(); });
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
        status.setText("Les médias accessibles de la galerie ont été supprimés. Les anciennes copies privées seront supprimées avec Minimal OS. Aucune sauvegarde n’a été effectuée.");
        if (PanicAutomation.enabled(this)) { removeSelf(); return; }
        new AlertDialog.Builder(this).setTitle("Dernière étape")
                .setMessage("Revenir au launcher constructeur et désinstaller Minimal OS ?")
                .setNegativeButton("Garder Minimal OS", null)
                .setPositiveButton("Désinstaller Minimal OS", (dialog, which) -> removeSelf()).show();
    }

    private void removeSelf() {
        if (!hasOtherHome()) { status.setText("Launcher constructeur indisponible : suppression interrompue."); return; }
        BootReceiver.retireTransfers(this);
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        android.content.pm.ResolveInfo chosen = null;
        for (android.content.pm.ResolveInfo item : getPackageManager().queryIntentActivities(home, 0)) {
            if (item.activityInfo == null || item.activityInfo.packageName.equals(getPackageName())
                    || !item.activityInfo.enabled || !item.activityInfo.applicationInfo.enabled) continue;
            if (chosen == null || (item.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) chosen = item;
        }
        if (chosen == null) return;
        home.setComponent(new android.content.ComponentName(chosen.activityInfo.packageName, chosen.activityInfo.name));
        try {
            startActivity(home);
            Intent delete = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + getPackageName()));
            PanicAutomation.uninstall(this, delete, getPackageName()); removingSelf = true;
            startActivity(delete);
        } catch (RuntimeException error) { PanicAutomation.clear(); status.setText("Dernière étape interrompue : " + error.getMessage()); }
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
        PanicAutomation.clear();
        if (aborted) return;
        if (request == UNINSTALL) {
            if (currentPackage == null) { status.setText("Procédure interrompue. Relancez Panic pour vérifier l’état actuel."); return; }
            try {
                getPackageManager().getPackageInfo(currentPackage, 0);
                status.setText("Panic arrêté : " + currentPackage + " est toujours installée.");
            } catch (PackageManager.NameNotFoundException removed) { nextUninstall(); }
        } else if (request == DELETE_MEDIA) {
            if (result != RESULT_OK) { status.setText("Panic arrêté : suppression de la galerie refusée."); return; }
            prepareGalleryDelete();
        } else if (request == LEGACY_DELETE) {
            if (result != RESULT_OK) status.setText("Panic arrêté : suppression refusée.");
            else prepareGalleryDelete();
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

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putStringArrayList("apps", new ArrayList<>(uninstallQueue));
        out.putString("current", currentPackage); out.putInt("batch", batchCount);
        out.putBoolean("start", start.isEnabled()); out.putString("status", status.getText().toString());
    }

    @Override public void onBackPressed() { aborted = true; PanicAutomation.clear(); super.onBackPressed(); }

    @Override protected void onDestroy() {
        aborted = true;
        if (!removingSelf) PanicAutomation.clear();
        super.onDestroy();
    }
}
