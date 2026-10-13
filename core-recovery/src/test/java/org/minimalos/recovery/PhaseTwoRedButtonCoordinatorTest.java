package org.minimalos.recovery;

import static org.junit.Assert.*;
import org.junit.Test;

public class PhaseTwoRedButtonCoordinatorTest {
    @Test public void blocksWhenAppProvenanceIsUnknown() {
        RecoveryStatus result = new PhaseTwoRedButtonCoordinator(
                new PanicPreflight(true, true, false, true)).prepareExit();
        assertFalse(result.available());
        assertTrue(result.message().contains("Provenance"));
        assertTrue(result.message().contains("Aucune application"));
    }

    @Test public void phaseTwoNeverExecutesDeletionEvenAfterPreflight() {
        RecoveryStatus result = new PhaseTwoRedButtonCoordinator(
                new PanicPreflight(true, true, true, true)).prepareExit();
        assertFalse(result.available());
        assertTrue(result.message().contains("Phase 4"));
    }
}
