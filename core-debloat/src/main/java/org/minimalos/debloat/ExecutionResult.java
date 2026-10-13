package org.minimalos.debloat;

import java.util.Objects;

public final class ExecutionResult {
    private final boolean changedSystem;
    private final String message;

    public ExecutionResult(boolean changedSystem, String message) {
        this.changedSystem = changedSystem;
        this.message = Objects.requireNonNull(message);
    }

    public boolean changedSystem() { return changedSystem; }
    public String message() { return message; }
}
