package org.minimalos.recovery;

import static org.junit.Assert.*;
import org.junit.Test;

public class UnavailableRedButtonCoordinatorTest {
    @Test public void phaseOneCannotClaimRecovery() {
        RecoveryStatus status = new UnavailableRedButtonCoordinator().prepareExit();
        assertFalse(status.available());
        assertTrue(status.message().contains("aucune désinstallation"));
    }
}
