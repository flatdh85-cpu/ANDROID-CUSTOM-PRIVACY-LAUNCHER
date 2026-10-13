package org.minimalos.replication;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class SyncResult {
    private final int sent;
    private final int received;
    private final Set<String> confirmedOnPeer;
    public SyncResult(int sent, int received, Set<String> confirmedOnPeer) {
        this.sent = sent;
        this.received = received;
        this.confirmedOnPeer = Collections.unmodifiableSet(new HashSet<>(confirmedOnPeer));
    }
    public int sent() { return sent; }
    public int received() { return received; }
    public Set<String> confirmedOnPeer() { return confirmedOnPeer; }
}
