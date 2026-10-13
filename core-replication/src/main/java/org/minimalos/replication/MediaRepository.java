package org.minimalos.replication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/** A received file is published only after finish() has verified and synced it. */
public interface MediaRepository {
    List<MediaRecord> list() throws IOException;
    InputStream open(MediaRecord record) throws IOException;
    PendingWrite begin(MediaRecord record) throws IOException;

    interface PendingWrite extends AutoCloseable {
        long resumeOffset() throws IOException;
        InputStream existing() throws IOException;
        OutputStream output() throws IOException;
        void finish(long size, String sha256) throws IOException;
        void discard() throws IOException;
        @Override void close() throws IOException;
    }
}
