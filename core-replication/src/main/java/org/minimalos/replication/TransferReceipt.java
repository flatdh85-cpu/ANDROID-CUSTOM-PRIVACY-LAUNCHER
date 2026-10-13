package org.minimalos.replication;

import java.util.Objects;

public final class TransferReceipt {
    private final String mediaId;
    private final long receivedSize;
    private final String receivedSha256;
    private final boolean durablyStored;

    public TransferReceipt(String mediaId, long receivedSize, String receivedSha256,
            boolean durablyStored) {
        this.mediaId = Objects.requireNonNull(mediaId);
        this.receivedSize = receivedSize;
        this.receivedSha256 = Objects.requireNonNull(receivedSha256);
        this.durablyStored = durablyStored;
    }

    public boolean verifies(MediaRecord expected) {
        return durablyStored
                && mediaId.equals(expected.id())
                && receivedSize == expected.size()
                && receivedSha256.equalsIgnoreCase(expected.sha256());
    }
}
