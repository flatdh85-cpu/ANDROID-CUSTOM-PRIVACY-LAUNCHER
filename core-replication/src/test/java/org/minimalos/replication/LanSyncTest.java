package org.minimalos.replication;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.FutureTask;
import org.junit.Test;

public class LanSyncTest {
    @Test public void copiesBothWaysAndRecoversDeletedLocalCopy() throws Exception {
        Memory a = new Memory();
        Memory b = new Memory();
        a.put("A1"); a.put("A2"); b.put("B1"); b.put("B2");
        byte[] key = LanSync.parseKey(LanSync.newKey());
        SyncResult first = exchange(a, b, key, key);
        assertEquals(2, first.sent());
        assertEquals(2, first.received());
        assertSameFiles(a, b);
        String removed = a.files.keySet().iterator().next();
        a.files.remove(removed);
        exchange(a, b, key, key);
        assertSameFiles(a, b);
        assertTrue(a.files.containsKey(removed));
        SyncResult again = exchange(a, b, key, key);
        assertEquals(0, again.sent());
        assertEquals(0, again.received());
    }

    @Test public void rejectsWrongPairCodeBeforeManifest() throws Exception {
        Memory a = new Memory();
        Memory b = new Memory();
        a.put("confidentiel");
        try {
            exchange(a, b, LanSync.parseKey(LanSync.newKey()), LanSync.parseKey(LanSync.newKey()));
            fail("Pair non authentifié accepté");
        } catch (IOException expected) { assertTrue(b.files.isEmpty()); }
    }

    @Test public void rejectsPathLikeMediaIdentifier() throws Exception {
        Memory a = new Memory();
        Memory b = new Memory();
        a.files.put("../outside", "x".getBytes(StandardCharsets.UTF_8));
        byte[] key = LanSync.parseKey(LanSync.newKey());
        try {
            exchange(a, b, key, key);
            fail("Identifiant non sûr accepté");
        } catch (IOException expected) { assertTrue(b.files.isEmpty()); }
    }

    @Test public void resumesFromVerifiedChunkBoundary() throws Exception {
        Memory a = new Memory();
        Memory b = new Memory();
        byte[] video = new byte[65536 * 2 + 19];
        for (int i = 0; i < video.length; i++) video[i] = (byte) i;
        String id = hash(video);
        a.files.put(id, video);
        b.partials.put(id, Arrays.copyOf(video, 65536));
        byte[] key = LanSync.parseKey(LanSync.newKey());
        exchange(a, b, key, key);
        assertArrayEquals(video, b.files.get(id));
        assertEquals(video.length - 65536, b.writtenBytes);
    }

    private static SyncResult exchange(Memory a, Memory b, byte[] clientKey, byte[] serverKey) throws Exception {
        try (ServerSocket listener = new ServerSocket(0)) {
            FutureTask<Void> server = new FutureTask<>(() -> {
                try (Socket socket = listener.accept()) {
                    LanSync.server(socket, serverKey, b);
                }
                return null;
            });
            new Thread(server).start();
            try (Socket socket = new Socket("127.0.0.1", listener.getLocalPort())) {
                SyncResult result = LanSync.client(socket, clientKey, a);
                server.get();
                return result;
            } catch (Exception failure) {
                try { server.get(); } catch (Exception ignored) { }
                throw failure;
            }
        }
    }

    private static void assertSameFiles(Memory a, Memory b) {
        assertEquals(a.files.keySet(), b.files.keySet());
        for (String id : a.files.keySet()) assertArrayEquals(a.files.get(id), b.files.get(id));
    }

    private static final class Memory implements MediaRepository {
        final Map<String, byte[]> files = new LinkedHashMap<>();
        final Map<String, byte[]> partials = new LinkedHashMap<>();
        int writtenBytes;
        void put(String text) throws Exception {
            byte[] data = text.getBytes(StandardCharsets.UTF_8);
            files.put(hash(data), data);
        }
        @Override public List<MediaRecord> list() {
            List<MediaRecord> records = new ArrayList<>();
            files.forEach((id, data) -> records.add(new MediaRecord(id, data.length, hash(data), "test")));
            return records;
        }
        @Override public InputStream open(MediaRecord record) { return new ByteArrayInputStream(files.get(record.id())); }
        @Override public PendingWrite begin(MediaRecord record) {
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            byte[] earlier = partials.getOrDefault(record.id(), new byte[0]);
            data.write(earlier, 0, earlier.length);
            return new PendingWrite() {
                @Override public long resumeOffset() { return earlier.length; }
                @Override public InputStream existing() { return new ByteArrayInputStream(earlier); }
                @Override public OutputStream output() {
                    return new OutputStream() {
                        @Override public void write(int item) { data.write(item); writtenBytes++; }
                        @Override public void write(byte[] bytes, int offset, int length) { data.write(bytes, offset, length); writtenBytes += length; }
                    };
                }
                @Override public void finish(long size, String sha256) throws IOException {
                    if (size != data.size() || !sha256.equals(hash(data.toByteArray()))) throw new IOException("Invalid file");
                    files.put(record.id(), data.toByteArray());
                    partials.remove(record.id());
                }
                @Override public void discard() { partials.remove(record.id()); }
                @Override public void close() { if (!files.containsKey(record.id())) partials.put(record.id(), data.toByteArray()); }
            };
        }
    }

    private static String hash(byte[] data) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder text = new StringBuilder();
            for (byte item : digest) text.append(String.format("%02x", item & 0xff));
            return text.toString();
        } catch (Exception impossible) { throw new IllegalStateException(impossible); }
    }
}
