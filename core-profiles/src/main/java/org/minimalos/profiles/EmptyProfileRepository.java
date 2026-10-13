package org.minimalos.profiles;

import org.minimalos.device.DeviceInfo;

public final class EmptyProfileRepository implements ProfileRepository {
    @Override public ProfileMatch findExact(DeviceInfo deviceInfo) {
        return ProfileMatch.missing("Aucun profil vérifié n'est livré en Phase 1");
    }
}
