package org.minimalos.debloat;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.minimalos.device.DeviceInfo;
import org.minimalos.device.PackageState;
import org.minimalos.profiles.DeviceProfile;
import org.minimalos.profiles.PackageClassification;
import org.minimalos.profiles.ProfilePackageRule;
import org.minimalos.profiles.StrictProfileRepository;

public class ProfileAwareDebloatPlannerTest {
    private static ProfilePackageRule rule(String packageName) {
        return new ProfilePackageRule(packageName, PackageClassification.CANDIDATE,
                "validated", Collections.emptyList(), "low", "disable-user", "enable",
                "device", "exact firmware", Collections.singletonList("call"));
    }

    private static DeviceProfile profile() {
        return new DeviceProfile(1, "exact", "Vendor", "Model", 29, "ROM", "fp",
                Arrays.asList(rule("com.vendor.optional"), rule("com.android.systemui"),
                        rule("com.vendor.launcher")));
    }

    @Test public void exactProfileOnlySimulatesSafeInstalledCandidates() {
        ProfileAwareDebloatPlanner planner = new ProfileAwareDebloatPlanner(
                new StrictProfileRepository(Collections.singletonList(profile())),
                new CriticalPackagePolicy());
        DebloatPlan plan = planner.plan(new DeviceInfo("vendor", "model", 29, "fp", "ROM"),
                Arrays.asList(new PackageState("com.vendor.optional", true, true),
                        new PackageState("com.android.systemui", true, true),
                        new PackageState("com.vendor.launcher", true, true)));
        assertTrue(plan.hasExactProfile());
        assertEquals(Collections.singletonList("disable-user:com.vendor.optional"), plan.packageActions());
        assertFalse(new AuditOnlyDebloatExecutor().execute(plan).changedSystem());
    }

    @Test public void fingerprintMismatchProducesNoActions() {
        ProfileAwareDebloatPlanner planner = new ProfileAwareDebloatPlanner(
                new StrictProfileRepository(Collections.singletonList(profile())),
                new CriticalPackagePolicy());
        DebloatPlan plan = planner.plan(new DeviceInfo("Vendor", "Model", 29, "other", "ROM"),
                Collections.singletonList(new PackageState("com.vendor.optional", true, true)));
        assertFalse(plan.hasExactProfile());
        assertTrue(plan.packageActions().isEmpty());
    }
}
