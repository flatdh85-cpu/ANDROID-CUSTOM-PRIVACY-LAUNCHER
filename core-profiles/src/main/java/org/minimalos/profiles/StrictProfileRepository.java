package org.minimalos.profiles;

import java.util.ArrayList;
import java.util.List;
import org.minimalos.device.DeviceInfo;

public final class StrictProfileRepository implements ProfileRepository {
    private final List<DeviceProfile> profiles;

    public StrictProfileRepository(List<DeviceProfile> profiles) {
        this.profiles = new ArrayList<>(profiles);
    }

    @Override public ProfileMatch findExact(DeviceInfo device) {
        for (DeviceProfile profile : profiles) {
            if (profile.manufacturer().equalsIgnoreCase(device.manufacturer())
                    && profile.model().equalsIgnoreCase(device.model())
                    && profile.sdkInt() == device.sdkInt()
                    && profile.vendorVersion().equals(device.vendorVersion())
                    && profile.fingerprint().equals(device.fingerprint())) {
                return ProfileMatch.exact(profile);
            }
        }
        return ProfileMatch.missing("Aucun profil exact pour ce fingerprint et cette version constructeur");
    }
}
