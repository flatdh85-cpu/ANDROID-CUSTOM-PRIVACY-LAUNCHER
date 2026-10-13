package org.minimalos.profiles;

import static org.junit.Assert.*;
import org.junit.Test;
import org.minimalos.device.DeviceInfo;

public class EmptyProfileRepositoryTest {
    @Test public void unknownDeviceNeverMatches() {
        DeviceInfo device = new DeviceInfo("Vendor", "Model", 21, "fingerprint", "unknown");
        assertFalse(new EmptyProfileRepository().findExact(device).isExact());
    }
}
