package org.minimalos.replication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReplicationPlan {
    private final List<MediaRecord> sendToRemote;
    private final List<MediaRecord> requestFromRemote;

    public ReplicationPlan(List<MediaRecord> sendToRemote, List<MediaRecord> requestFromRemote) {
        this.sendToRemote = Collections.unmodifiableList(new ArrayList<>(sendToRemote));
        this.requestFromRemote = Collections.unmodifiableList(new ArrayList<>(requestFromRemote));
    }

    public List<MediaRecord> sendToRemote() { return sendToRemote; }
    public List<MediaRecord> requestFromRemote() { return requestFromRemote; }
}
