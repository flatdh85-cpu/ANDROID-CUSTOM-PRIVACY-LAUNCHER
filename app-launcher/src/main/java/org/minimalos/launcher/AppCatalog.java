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
                new AppEntry("LocalSend", "org.localsend.localsend_app", R.drawable.ic_localsend),
                new AppEntry("BitChat", "com.bitchat.droid", R.drawable.ic_bitchat),
                new AppEntry("SimpleX", "chat.simplex.app", R.drawable.ic_simplex)
        ));
    }

    /** Minimum declared by current upstream releases; an already installed legacy app can still be opened. */
    public static int currentMinimumSdk(AppEntry entry) {
        return entry.packageName().equals("chat.simplex.app") || entry.packageName().equals("com.bitchat.droid") ? 26 : 21;
    }

    public static String resolvePackageName(Context context, AppEntry entry) {
        if (!entry.label().equals("BitChat")) return entry.packageName();
        PackageManager manager = context.getPackageManager();
        try { manager.getApplicationInfo(entry.packageName(), 0); return entry.packageName(); }
        catch (PackageManager.NameNotFoundException ignored) { }
        try { manager.getApplicationInfo("com.bitchat.android", 0); return "com.bitchat.android"; }
        catch (PackageManager.NameNotFoundException ignored) { }
        return entry.packageName();
    }
}
