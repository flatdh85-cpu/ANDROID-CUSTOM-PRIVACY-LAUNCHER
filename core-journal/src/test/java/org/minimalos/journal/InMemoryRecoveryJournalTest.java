package org.minimalos.journal;

import static org.junit.Assert.*;
import org.junit.Test;

public class InMemoryRecoveryJournalTest {
    @Test public void preservesOrderAndDoesNotExposeMutableStorage() {
        InMemoryRecoveryJournal journal = new InMemoryRecoveryJournal();
        journal.append("initial");
        journal.append("audit");
        assertArrayEquals(new String[] {"initial", "audit"}, journal.events().toArray());
        try {
            journal.events().add("unsafe");
            fail("events must be immutable");
        } catch (UnsupportedOperationException expected) {
            assertEquals(2, journal.events().size());
        }
    }
}
