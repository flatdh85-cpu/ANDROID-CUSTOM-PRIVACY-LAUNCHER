package org.minimalos.replication;

import java.util.Set;

public final class TransportSelector {
    private static final long SMALL_PAYLOAD = 256 * 1024;

    public Transport select(Set<Transport> mutualCapabilities, long payloadBytes) {
        if (mutualCapabilities.contains(Transport.WIFI_DIRECT)) return Transport.WIFI_DIRECT;
        if (mutualCapabilities.contains(Transport.LOCAL_HOTSPOT)) return Transport.LOCAL_HOTSPOT;
        if (mutualCapabilities.contains(Transport.WIFI_AWARE)) return Transport.WIFI_AWARE;
        if (payloadBytes <= SMALL_PAYLOAD
                && mutualCapabilities.contains(Transport.BLUETOOTH_CLASSIC)) {
            return Transport.BLUETOOTH_CLASSIC;
        }
        return Transport.NONE;
    }
}
