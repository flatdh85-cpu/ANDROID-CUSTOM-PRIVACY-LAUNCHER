package org.minimalos.journal;

import java.util.List;

public interface RecoveryJournal {
    void append(String event);
    List<String> events();
}
