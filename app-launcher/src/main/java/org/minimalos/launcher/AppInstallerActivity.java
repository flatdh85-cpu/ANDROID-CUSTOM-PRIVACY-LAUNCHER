package org.minimalos.launcher;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.widget.*;
import java.io.*;
import java.security.MessageDigest;

/** Foreground installation wizard; DownloadManager survives backgrounding, Android owns confirmations. */
@android.annotation.SuppressLint("SetTextI18n")
public final class AppInstallerActivity extends Activity {
    private TextView status;
    private boolean resumed, checking;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable poll = new Runnable() { public void run() { checkDownload(); if (resumed) handler.postDelayed(this, 1200); } };
    private static SharedPreferences prefs(Context context) { return context.getSharedPreferences("install-apps", MODE_PRIVATE); }
    private DownloadManager downloads() { return (DownloadManager) getSystemService(DOWNLOAD_SERVICE); }
    static void cancelPending(Context context) {
        SharedPreferences state = prefs(context); long id = state.getLong("download", -1);
        if (id != -1) try { ((DownloadManager) context.getSystemService(DOWNLOAD_SERVICE)).remove(id); }
        catch (RuntimeException unavailable) { android.util.Log.w("MinimalOS", "Gestionnaire de téléchargement indisponible"); }
        state.edit().remove("download").remove("asset").putBoolean("awaiting", false).apply();
    }
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setTitle("Configurer Minimal OS");
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        int padding = Ui.dp(this, 20); root.setPadding(padding,padding,padding,padding);
        root.addView(Ui.text(this, "SimpleX · BitChat · LocalSend", 24, -1));
        root.addView(Ui.text(this, "Téléchargement des APK officielles par Internet, puis installation avec Android. Les applications présentes sont conservées. Android demande l’autorisation de cette source et confirme chaque installation.\n\nLocalSend 1.17.0 · BitChat 2.0.1 · SimpleX 7.0.3. Sur Android 6, seul LocalSend est compatible parmi ces versions. Prévoir jusqu’à environ 170 Mo de téléchargement sur ARM64.", 16, -1));
        root.addView(Ui.text(this, "Autorisations Minimal OS : localisation, galerie et fichiers. Sur Android 11+, l’accès à tous les fichiers se règle dans un écran Android séparé. La localisation n’est ni lue ni transmise par ce launcher. La configuration du mode Panic reste séparée.", 15, -1));
        Button permissions = Ui.button(this, "Accorder / vérifier les autorisations");
        permissions.setOnClickListener(view -> requestSetupPermissions()); root.addView(permissions, Ui.matchWrap());
        Button install = Ui.button(this, "Installer / reprendre");
        install.setOnClickListener(view -> {
            prefs(this).edit().putBoolean("awaiting", false).apply();
            if (prefs(this).getLong("download", -1) == -1) next(); else checkDownload();
        }); root.addView(install, Ui.matchWrap());
        Button cancel = Ui.button(this, "Annuler les téléchargements");
        cancel.setOnClickListener(view -> { cancelPending(this); status.setText("Installation arrêtée. Les applications déjà installées sont conservées."); }); root.addView(cancel, Ui.matchWrap());
        status = Ui.text(this, permissionSummary() + "\n" + summary(), 16, -1); root.addView(status, Ui.matchWrap());
        Button later = Ui.button(this, "Retour à l’accueil"); later.setOnClickListener(view -> finish()); root.addView(later, Ui.matchWrap());
        ScrollView scroll = new ScrollView(this); scroll.addView(root); setContentView(scroll);
        if (state == null && !prefs(this).getBoolean("permissions-offered",false)) {
            prefs(this).edit().putBoolean("permissions-offered",true).apply();
            requestSetupPermissions();
        }
    }
    private void requestSetupPermissions() {
        if (Build.VERSION.SDK_INT < 23) { requestFilesAccess(); return; }
        java.util.List<String> wanted = new java.util.ArrayList<>();
        wanted.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        wanted.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= 33) {
            wanted.add(android.Manifest.permission.READ_MEDIA_IMAGES); wanted.add(android.Manifest.permission.READ_MEDIA_VIDEO);
            if (Build.VERSION.SDK_INT >= 34) wanted.add(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
        } else {
            wanted.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT <= 29) wanted.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        java.util.List<String> missing = new java.util.ArrayList<>();
        for (String permission : wanted) if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) missing.add(permission);
        if (missing.contains(android.Manifest.permission.ACCESS_FINE_LOCATION) && !missing.contains(android.Manifest.permission.ACCESS_COARSE_LOCATION))
            missing.add(android.Manifest.permission.ACCESS_COARSE_LOCATION); // Android 12+ requires requesting both together.
        if (missing.isEmpty()) { requestFilesAccess(); return; }
        requestPermissions(missing.toArray(new String[0]),203);
    }
    private void requestFilesAccess() {
        if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            status.setText("Activez l’accès à tous les fichiers pour l’effacement de Téléchargements, puis revenez ici. La configuration Panic reste séparée.");
            try { startActivityForResult(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,Uri.parse("package:"+getPackageName())),204); }
            catch (RuntimeException missing) {
                try { startActivityForResult(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION),204); }
                catch (RuntimeException unavailable) { status.setText("Accès spécial aux fichiers indisponible. Ouvrez les informations Android de Minimal OS."); }
            }
        } else status.setText(permissionSummary() + "\n" + summary());
    }
    private String permissionSummary() {
        if (Build.VERSION.SDK_INT < 23) return "Permissions accordées à l’installation.";
        boolean location = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean gallery = Build.VERSION.SDK_INT >= 33
                ? checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                : checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean files = Build.VERSION.SDK_INT >= 30 ? Environment.isExternalStorageManager() : checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return "Localisation : " + (location ? "accordée" : "non accordée") + "\nGalerie complète : " + (gallery ? "accordée" : "non accordée") + "\nFichiers : " + (files ? "accordés" : "non accordés");
    }
    @Override public void onRequestPermissionsResult(int request, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(request,permissions,results);
        if (request == 203) requestFilesAccess();
    }

    private boolean installed(String name) {
        try { getPackageManager().getPackageInfo(name,0); return true; } catch (PackageManager.NameNotFoundException absent) { return false; }
    }
    private String summary() {
        StringBuilder text = new StringBuilder();
        for (AppEntry app : AppCatalog.entries()) {
            boolean present = installed(AppCatalog.resolvePackageName(this, app));
            text.append(app.label()).append(" : ").append(present ? "déjà installée" : AppInstallCatalog.select(app.packageName(), Build.VERSION.SDK_INT, Build.SUPPORTED_ABIS) == null ? "version non compatible avec cet Android/processeur" : "prête à installer").append('\n');
        }
        return text.toString();
    }
    private boolean sourceAllowed() {
        if (Build.VERSION.SDK_INT >= 26 && !getPackageManager().canRequestPackageInstalls()) {
            status.setText("Autorisez Minimal OS comme source d’installation, puis revenez ici.");
            prefs(this).edit().putBoolean("awaiting",true).apply();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName())), 202);
            return false;
        }
        return true;
    }
    private void next() {
        try {
            if (!sourceAllowed()) return;
            for (AppEntry entry : AppCatalog.entries()) {
                if (installed(AppCatalog.resolvePackageName(this, entry))) continue;
                AppInstallCatalog.Asset asset = AppInstallCatalog.select(entry.packageName(), Build.VERSION.SDK_INT, Build.SUPPORTED_ABIS);
                if (asset == null) continue;
                File target = file(asset); if (target.exists() && !target.delete()) throw new IOException("Ancien téléchargement non supprimé");
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(asset.url))
                        .setTitle("Minimal OS · " + asset.label).setMimeType("application/vnd.android.package-archive")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                        .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, asset.key + ".apk");
                long id = downloads().enqueue(request);
                prefs(this).edit().putLong("download", id).putString("asset", asset.key).putBoolean("awaiting",false).apply();
                status.setText("Téléchargement de " + asset.label + "…"); return;
            }
            status.setText("Installation des applications compatibles terminée.\n\n" + summary());
        } catch (Exception error) { status.setText("Installation interrompue : " + error.getMessage()); }
    }
    private File file(AppInstallCatalog.Asset asset) throws IOException {
        File folder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (folder == null) throw new IOException("Stockage indisponible");
        return new File(folder, asset.key + ".apk");
    }
    private void checkDownload() {
        if (!resumed || checking || prefs(this).getBoolean("awaiting",false)) return;
        long id = prefs(this).getLong("download", -1); if (id == -1) return;
        AppInstallCatalog.Asset asset = AppInstallCatalog.find(prefs(this).getString("asset", ""));
        if (asset == null) { cancelPending(this); return; }
        try (Cursor cursor = downloads().query(new DownloadManager.Query().setFilterById(id))) {
            if (cursor == null || !cursor.moveToFirst()) { cancelPending(this); status.setText("Téléchargement supprimé. Touchez Installer / reprendre."); return; }
            int result = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            if (result == DownloadManager.STATUS_FAILED) {
                int reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));
                cancelPending(this); status.setText("Téléchargement échoué : " + reason + ". Vérifiez Internet puis réessayez."); return;
            }
            if (result != DownloadManager.STATUS_SUCCESSFUL) {
                long bytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                status.setText(asset.label + " : " + bytes / 1048576 + " Mo téléchargés" + (result == DownloadManager.STATUS_PAUSED ? " · en attente du réseau" : "")); return;
            }
        } catch (Exception error) { status.setText("Téléchargement inaccessible : " + error.getMessage()); return; }
        checking = true; status.setText("Contrôle de l’APK " + asset.label + "…");
        new Thread(() -> {
            try {
                File apk = file(asset); MessageDigest digest = MessageDigest.getInstance("SHA-256");
                try (InputStream input = new FileInputStream(apk)) { byte[] buffer = new byte[65536]; int n; while ((n=input.read(buffer))!=-1) digest.update(buffer,0,n); }
                if (!hex(digest.digest()).equals(asset.sha256)) throw new IOException("Empreinte de l’APK incorrecte");
                PackageInfo info = getPackageManager().getPackageArchiveInfo(apk.getAbsolutePath(), PackageManager.GET_SIGNATURES);
                if (info == null || !asset.packageName.equals(info.packageName) || info.signatures == null || info.signatures.length != 1
                        || !hex(MessageDigest.getInstance("SHA-256").digest(info.signatures[0].toByteArray())).equals(asset.certificate)) throw new IOException("Identité/signature de l’APK incorrecte");
                runOnUiThread(() -> {
                    checking = false;
                    if (!resumed || prefs(this).getLong("download",-1) != id) return;
                    try {
                        if (!sourceAllowed()) return;
                        Uri uri = downloads().getUriForDownloadedFile(id); if (uri == null) throw new IOException("APK inaccessible");
                        Intent install = new Intent(Intent.ACTION_INSTALL_PACKAGE, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).putExtra(Intent.EXTRA_RETURN_RESULT,true);
                        prefs(this).edit().putBoolean("awaiting",true).apply();
                        startActivityForResult(install,201);
                    } catch (Exception error) { prefs(this).edit().putBoolean("awaiting",true).apply(); status.setText("Installation indisponible : " + error.getMessage()); }
                });
            } catch (Exception error) {
                runOnUiThread(() -> { checking = false; if (prefs(this).getLong("download",-1) == id) { cancelPending(this); status.setText("APK refusée : " + error.getMessage()); } });
            }
        }, "apk-verify").start();
    }
    private static String hex(byte[] bytes) { StringBuilder value = new StringBuilder(); for (byte b : bytes) value.append(String.format(java.util.Locale.ROOT,"%02x",b & 255)); return value.toString(); }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request,result,data);
        if (request == 204) { status.setText(permissionSummary() + "\n" + summary()); return; }
        if (request == 202) {
            boolean allowed = Build.VERSION.SDK_INT >= 26 && getPackageManager().canRequestPackageInstalls();
            prefs(this).edit().putBoolean("awaiting",!allowed).apply();
            if (allowed && prefs(this).getLong("download",-1) == -1) next();
            if (!allowed) status.setText("Source non autorisée. Touchez Installer / reprendre pour réessayer.");
            return;
        }
        if (request == 201) {
            AppInstallCatalog.Asset asset = AppInstallCatalog.find(prefs(this).getString("asset",""));
            boolean success = asset != null && installed(asset.packageName); cancelPending(this);
            if (success) next(); else status.setText("Installation refusée ou échouée. Touchez Installer / reprendre pour réessayer.\n" + summary());
        }
    }
    @Override protected void onResume() { super.onResume(); resumed=true; handler.post(poll); }
    @Override protected void onPause() { resumed=false; handler.removeCallbacks(poll); super.onPause(); }
}
