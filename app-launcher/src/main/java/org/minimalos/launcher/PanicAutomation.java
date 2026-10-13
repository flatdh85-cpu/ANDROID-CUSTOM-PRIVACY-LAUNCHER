package org.minimalos.launcher;

import android.content.*;
import android.content.pm.*;
import android.os.SystemClock;
import android.provider.Settings;

/** Single in-memory authorization for the next known system confirmation, expires after 20 seconds. */
final class PanicAutomation {
    static final class Expected {
        final String kind, systemPackage, label;
        final long expires = SystemClock.elapsedRealtime() + 20000;
        Expected(String kind, String systemPackage, String label) { this.kind = kind; this.systemPackage = systemPackage; this.label = label; }
    }
    private static Expected expected;
    static boolean enabled(Context context) {
        if (!context.getSharedPreferences("panic", Context.MODE_PRIVATE).getBoolean("automatic", false)) return false;
        String services = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (services == null) return false;
        ComponentName own = new ComponentName(context, PanicAccessibilityService.class);
        for (String service : services.split(":")) if (own.equals(ComponentName.unflattenFromString(service))) return true;
        return false;
    }
    static synchronized void expect(Context context, String kind, String systemPackage, String label) {
        expected = null;
        if (!enabled(context) || systemPackage == null) return;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(systemPackage, 0);
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) return;
            expected = new Expected(kind, systemPackage, label);
        } catch (PackageManager.NameNotFoundException missing) { }
    }
    static void uninstall(Context context, Intent intent, String target) {
        ResolveInfo resolved = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        try {
            String label = context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(target, 0)).toString();
            expect(context, "uninstall", resolved == null || resolved.activityInfo == null ? null : resolved.activityInfo.packageName, label);
        } catch (PackageManager.NameNotFoundException missing) { clear(); }
    }
    static synchronized Expected current() {
        if (expected != null && SystemClock.elapsedRealtime() > expected.expires) expected = null;
        return expected;
    }
    static synchronized boolean consume(Expected candidate) {
        if (current() != candidate || candidate == null) return false;
        expected = null; return true;
    }
    static synchronized void clear() { expected = null; }
}
