package org.minimalos.launcher;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.Collection;
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
                result.add(new PackageState(packageName, true, info.enabled));
            } catch (PackageManager.NameNotFoundException missing) {
                result.add(new PackageState(packageName, false, false));
            }
        }
        return result;
    }
}
