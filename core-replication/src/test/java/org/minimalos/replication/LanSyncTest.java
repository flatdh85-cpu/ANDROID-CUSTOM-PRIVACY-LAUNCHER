package org.minimalos.replication;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
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
    @Test public void copiesBothWaysOverBluetoothStyleStreams() throws Exception {
        Memory a = new Memory();
        Memory b = new Memory();
        a.put("photo A");
        b.put("vidéo B");
        byte[] key = LanSync.parseKey(LanSync.newKey());
        try (PipedInputStream clientIn = new PipedInputStream(131072);
                PipedInputStream serverIn = new PipedInputStream(131072);
                PipedOutputStream clientOut = new PipedOutputStream(serverIn);
                PipedOutputStream serverOut = new PipedOutputStream(clientIn)) {
            FutureTask<SyncResult> server = new FutureTask<>(() -> LanSync.server(serverIn, serverOut, key, b));
            new Thread(server).start();
            SyncResult client = LanSync.client(clientIn, clientOut, key, a);
            server.get();
            assertEquals(1, client.sent());
            assertEquals(1, client.received());
            assertSameFiles(a, b);
        }
    }
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
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Code partagé"));
            assertTrue(b.files.isEmpty());
        }
    }

    @Test public void reportsReceiverGalleryFailureInsteadOfClosingWithoutReason() throws Exception {
        Memory a = new Memory();
        a.put("A1");
        MediaRepository unavailable = new MediaRepository() {
            @Override public List<MediaRecord> list() throws IOException { throw new IOException("Accès galerie refusé"); }
            @Override public InputStream open(MediaRecord record) throws IOException { throw new IOException("Indisponible"); }
            @Override public PendingWrite begin(MediaRecord record) throws IOException { throw new IOException("Indisponible"); }
        };
        byte[] key = LanSync.parseKey(LanSync.newKey());
        try {
            exchange(a, unavailable, key, key);
            fail("La galerie indisponible du récepteur a été ignorée");
        } catch (IOException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("Galerie du pair indisponible"));
            assertTrue(expected.getMessage().contains("Accès galerie refusé"));
        }
    }

    @Test public void panicProbeRequiresTheSameCode() throws Exception {
        byte[] receiverKey = LanSync.parseKey(LanSync.newKey());
        try (ServerSocket listener = new ServerSocket(0)) {
            FutureTask<Void> server = new FutureTask<>(() -> {
                try (Socket socket = listener.accept()) {
                    try { LanSync.server(socket, receiverKey, new Memory()); }
                    catch (IOException closedAfterProbe) { /* Probe deliberately stops before a manifest. */ }
                }
                return null;
            });
            new Thread(server).start();
            try (Socket socket = new Socket("127.0.0.1", listener.getLocalPort())) {
                LanSync.probeClient(socket.getInputStream(), socket.getOutputStream(), receiverKey);
            }
            server.get();
        }
        try (ServerSocket listener = new ServerSocket(0)) {
            FutureTask<Void> server = new FutureTask<>(() -> {
                try (Socket socket = listener.accept()) {
                    try { LanSync.server(socket, receiverKey, new Memory()); }
                    catch (IOException wrongCode) { /* Receiver rejects the client. */ }
                }
                return null;
            });
            new Thread(server).start();
            try (Socket socket = new Socket("127.0.0.1", listener.getLocalPort())) {
                try {
                    LanSync.probeClient(socket.getInputStream(), socket.getOutputStream(),
                            LanSync.parseKey(LanSync.newKey()));
                    fail("Mauvais code accepté par Panic");
                } catch (IOException expected) { /* Receiver closes on wrong key. */ }
            }
            server.get();
        }
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

    private static SyncResult exchange(Memory a, MediaRepository b, byte[] clientKey, byte[] serverKey) throws Exception {
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
