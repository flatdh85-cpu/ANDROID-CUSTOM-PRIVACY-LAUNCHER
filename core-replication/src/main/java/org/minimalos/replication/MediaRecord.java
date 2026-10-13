package org.minimalos.replication;

import java.util.Objects;

public final class MediaRecord {
    private final String id;
    private final long size;
    private final String sha256;
    private final String originDevice;

    public MediaRecord(String id, long size, String sha256, String originDevice) {
        if (size < 0) throw new IllegalArgumentException("Taille négative");
        this.id = requireText(id, "id");
        this.size = size;
        this.sha256 = validateSha256(sha256);
        this.originDevice = requireText(originDevice, "originDevice");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(name);
        return value;
    }

    private static String validateSha256(String value) {
        String normalized = requireText(value, "sha256").toLowerCase();
        if (normalized.length() != 64) throw new IllegalArgumentException("sha256");
        for (int index = 0; index < normalized.length(); index++) {
            if (Character.digit(normalized.charAt(index), 16) < 0) {
                throw new IllegalArgumentException("sha256");
            }
        }
        return normalized;
    }

    public String id() { return id; }
    public long size() { return size; }
    public String sha256() { return sha256; }
    public String originDevice() { return originDevice; }
}
