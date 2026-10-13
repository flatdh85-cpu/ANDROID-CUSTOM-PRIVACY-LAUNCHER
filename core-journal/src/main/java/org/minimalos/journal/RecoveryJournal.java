package org.minimalos.journal;

import java.util.List;

public interface RecoveryJournal {
    void append(String event);
    List<String> events();

    default String exportText() {
        StringBuilder output = new StringBuilder();
        for (String event : events()) output.append(event).append('\n');
        return output.toString();
    }
}
