package org.minimalos.replication;

import java.util.ArrayList;
import java.util.List;

public final class ReplicationPlanner {
    public ReplicationPlan compare(ReplicationManifest local, ReplicationManifest remote) {
        List<MediaRecord> send = new ArrayList<>();
        for (MediaRecord record : local.records()) {
            if (!remote.contains(record.id())) send.add(record);
        }
        List<MediaRecord> request = new ArrayList<>();
        for (MediaRecord record : remote.records()) {
            if (!local.contains(record.id())) request.add(record);
        }
        return new ReplicationPlan(send, request);
    }
}
