package org.minimalos.replication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReplicationManifest {
    private final Map<String, MediaRecord> byId;

    public ReplicationManifest(List<MediaRecord> records) {
        Map<String, MediaRecord> values = new LinkedHashMap<>();
        for (MediaRecord record : records) {
            if (values.put(record.id(), record) != null) {
                throw new IllegalArgumentException("Identifiant média dupliqué : " + record.id());
            }
        }
        byId = Collections.unmodifiableMap(values);
    }

    public boolean contains(String id) { return byId.containsKey(id); }
    public List<MediaRecord> records() { return new ArrayList<>(byId.values()); }
}
