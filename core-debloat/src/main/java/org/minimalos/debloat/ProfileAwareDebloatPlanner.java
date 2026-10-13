package org.minimalos.debloat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.minimalos.device.DeviceInfo;
import org.minimalos.device.PackageState;
import org.minimalos.profiles.DeviceProfile;
import org.minimalos.profiles.PackageClassification;
import org.minimalos.profiles.ProfileMatch;
import org.minimalos.profiles.ProfilePackageRule;
import org.minimalos.profiles.ProfileRepository;

public final class ProfileAwareDebloatPlanner implements DebloatPlanner {
    private final ProfileRepository profiles;
    private final CriticalPackagePolicy criticalPolicy;

    public ProfileAwareDebloatPlanner(ProfileRepository profiles, CriticalPackagePolicy criticalPolicy) {
        this.profiles = profiles;
        this.criticalPolicy = criticalPolicy;
    }

    @Override public DebloatPlan plan(DeviceInfo device, List<PackageState> packages) {
        ProfileMatch match = profiles.findExact(device);
        if (!match.isExact() || match.profile() == null) {
            return new DebloatPlan(false, new ArrayList<>(),
                    match.reason() + " — mode audit, aucune action proposée");
        }
        Set<String> installed = new HashSet<>();
        for (PackageState state : packages) {
            if (state.installed()) installed.add(state.packageName());
        }
        DeviceProfile profile = match.profile();
        List<String> actions = new ArrayList<>();
        for (ProfilePackageRule rule : profile.packages()) {
            if (rule.classification() == PackageClassification.CANDIDATE
                    && installed.contains(rule.packageName())
                    && !criticalPolicy.isProtected(rule.packageName())) {
                actions.add(rule.method() + ":" + rule.packageName());
            }
        }
        return new DebloatPlan(true, actions,
                "Simulation du profil exact " + profile.profileId() + " ; aucune exécution en Phase 2");
    }
}
