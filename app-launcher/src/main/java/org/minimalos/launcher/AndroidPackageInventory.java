package org.minimalos.launcher;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.minimalos.device.PackageInventory;
import org.minimalos.device.PackageState;

public final class AndroidPackageInventory implements PackageInventory {
    private final PackageManager packageManager;

    public AndroidPackageInventory(Context context) {
        packageManager = context.getPackageManager();
    }

    @Override public List<PackageState> inspect(Collection<String> packageNames) {
        List<PackageState> result = new ArrayList<>();
        for (String packageName : packageNames) {
            try {
                ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
                result.add(toState(info));
            } catch (PackageManager.NameNotFoundException missing) {
                result.add(new PackageState(packageName, false, false));
            }
        }
        return result;
    }

    @Override public List<PackageState> inspectAll() {
        List<PackageState> result = new ArrayList<>();
        for (ApplicationInfo info : packageManager.getInstalledApplications(0)) {
            result.add(toState(info));
        }
        Collections.sort(result, (left, right) -> left.packageName().compareTo(right.packageName()));
        return result;
    }

    @SuppressWarnings("deprecation")
    private PackageState toState(ApplicationInfo info) {
        String versionName = "";
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(info.packageName, 0);
            if (packageInfo.versionName != null) versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
            // L'application a pu être retirée entre les deux lectures de l'inventaire.
        }
        boolean system = (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        return new PackageState(info.packageName, true, info.enabled, system, versionName);
    }
}
