package org.minimalos.launcher;

import android.os.Build;
import org.minimalos.device.DeviceDetector;
import org.minimalos.device.DeviceInfo;

public final class AndroidDeviceDetector implements DeviceDetector {
    @Override public DeviceInfo detect() {
        String vendorVersion = Build.DISPLAY == null ? "inconnue" : Build.DISPLAY;
        return new DeviceInfo(safe(Build.MANUFACTURER), safe(Build.MODEL),
                Build.VERSION.SDK_INT, safe(Build.FINGERPRINT), vendorVersion);
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "inconnu" : value;
    }
}
