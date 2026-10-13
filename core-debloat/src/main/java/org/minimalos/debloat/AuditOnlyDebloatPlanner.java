package org.minimalos.debloat;

import java.util.Collections;
import java.util.List;
import org.minimalos.device.DeviceInfo;
import org.minimalos.device.PackageState;

public final class AuditOnlyDebloatPlanner implements DebloatPlanner {
    @Override public DebloatPlan plan(DeviceInfo device, List<PackageState> packages) {
        return new DebloatPlan(false, Collections.emptyList(),
                "Mode audit Phase 1 : aucun profil exact, aucune action autorisée");
    }
}
