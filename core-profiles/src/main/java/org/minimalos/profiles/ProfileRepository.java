package org.minimalos.profiles;

import org.minimalos.device.DeviceInfo;

public interface ProfileRepository {
    ProfileMatch findExact(DeviceInfo deviceInfo);
}
