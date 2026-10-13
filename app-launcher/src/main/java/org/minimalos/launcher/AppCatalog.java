package org.minimalos.launcher;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AppCatalog {
    private AppCatalog() {}

    public static List<AppEntry> entries() {
        return Collections.unmodifiableList(Arrays.asList(
                new AppEntry("Telegram", "org.telegram.messenger", R.drawable.ic_telegram),
                new AppEntry("Canta", "org.samo_lego.canta", R.drawable.ic_canta),
                new AppEntry("Shizuku", "moe.shizuku.privileged.api", R.drawable.ic_shizuku),
                new AppEntry("SimpleX Chat", "chat.simplex.app", R.drawable.ic_simplex),
                new AppEntry("Briar", "org.briarproject.briar.android", R.drawable.ic_briar),
                new AppEntry("LocalSend", "org.localsend.localsend_app", R.drawable.ic_localsend),
                new AppEntry("Bridgefy", "me.bridgefy.main", R.drawable.ic_launcher),
                new AppEntry("BitChat", "com.bitchat.android", R.drawable.ic_launcher)
        ));
    }

    public static String resolvePackageName(Context context, AppEntry entry) {
        if (!entry.label().equals("Bridgefy") && !entry.label().equals("BitChat")) return entry.packageName();
        PackageManager manager = context.getPackageManager();
        try { manager.getApplicationInfo(entry.packageName(), 0); return entry.packageName(); }
        catch (PackageManager.NameNotFoundException ignored) { }
        String token = entry.label().toLowerCase(java.util.Locale.ROOT);
        for (ApplicationInfo app : manager.getInstalledApplications(0)) {
            String label = manager.getApplicationLabel(app).toString().toLowerCase(java.util.Locale.ROOT);
            if (label.equals(token) || label.startsWith(token + " ")) return app.packageName;
        }
        return entry.packageName();
    }
}
