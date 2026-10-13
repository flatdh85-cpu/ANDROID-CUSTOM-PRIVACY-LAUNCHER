package org.minimalos.journal;

import static org.junit.Assert.*;
import java.io.File;
import java.io.RandomAccessFile;
import org.junit.Test;

public class FileRecoveryJournalTest {
    @Test public void survivesReloadAndIgnoresInterruptedLastFrame() throws Exception {
        File file = File.createTempFile("minimal-os-journal", ".bin");
        file.deleteOnExit();
        FileRecoveryJournal journal = new FileRecoveryJournal(file);
        journal.append("device-snapshot");
        journal.append("audit-plan");

        FileRecoveryJournal reloaded = new FileRecoveryJournal(file);
        assertEquals(2, reloaded.events().size());

        try (RandomAccessFile damaged = new RandomAccessFile(file, "rw")) {
            damaged.setLength(damaged.length() - 5);
        }
        assertEquals(1, reloaded.events().size());
        assertEquals("device-snapshot", reloaded.events().get(0));
    }

    @Test public void exportsReadableEvents() throws Exception {
        File file = File.createTempFile("minimal-os-export", ".bin");
        file.deleteOnExit();
        FileRecoveryJournal journal = new FileRecoveryJournal(file);
        journal.append("one");
        journal.append("two");
        assertEquals("one\ntwo\n", journal.exportText());
    }
}
