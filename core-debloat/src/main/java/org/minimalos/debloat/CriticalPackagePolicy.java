package org.minimalos.debloat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class CriticalPackagePolicy {
    private static final Set<String> EXACT = new HashSet<>(Arrays.asList(
            "android",
            "com.android.systemui",
            "com.android.settings",
            "com.android.phone",
            "com.android.providers.telephony",
            "com.android.providers.contacts",
            "com.android.permissioncontroller",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller"
    ));

    public boolean isProtected(String packageName) {
        String value = packageName.toLowerCase(Locale.ROOT);
        return EXACT.contains(value)
                || value.contains("inputmethod")
                || value.contains("launcher")
                || value.contains("keyguard")
                || value.contains("telephony")
                || value.contains("permissioncontroller");
    }
}
