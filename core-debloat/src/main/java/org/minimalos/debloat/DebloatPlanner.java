package org.minimalos.debloat;

import java.util.List;
import org.minimalos.device.DeviceInfo;
import org.minimalos.device.PackageState;

public interface DebloatPlanner {
    DebloatPlan plan(DeviceInfo device, List<PackageState> packages);
}
