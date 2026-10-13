package org.minimalos.device;

import static org.junit.Assert.*;
import org.junit.Test;

public class PackageStateTest {
    @Test public void representsUnavailablePackageWithoutClaimingItIsEnabled() {
        PackageState state = new PackageState("example.missing", false, false);
        assertFalse(state.installed());
        assertFalse(state.enabled());
        assertEquals("example.missing", state.packageName());
    }

    @Test public void representsVersionAndSystemOriginForAudit() {
        PackageState state = new PackageState("example.system", true, true, true, "2.4");
        assertTrue(state.systemPackage());
        assertEquals("2.4", state.versionName());
    }
}
