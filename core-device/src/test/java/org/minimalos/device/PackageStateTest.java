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
}
