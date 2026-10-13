package org.minimalos.recovery;

import java.util.Objects;

public final class RecoveryStatus {
    private final boolean available;
    private final String message;

    public RecoveryStatus(boolean available, String message) {
        this.available = available;
        this.message = Objects.requireNonNull(message);
    }

    public boolean available() { return available; }
    public String message() { return message; }
}
