package org.minimalos.journal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class InMemoryRecoveryJournal implements RecoveryJournal {
    private final List<String> entries = new ArrayList<>();

    @Override public synchronized void append(String event) {
        entries.add(Objects.requireNonNull(event));
    }

    @Override public synchronized List<String> events() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }
}
