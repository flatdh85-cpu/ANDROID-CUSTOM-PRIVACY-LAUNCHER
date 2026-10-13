package org.minimalos.replication;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
        } catch (LanSync.AuthenticationException expected) {
            assertTrue(expected.getMessage().contains("refusé le code"));
            assertTrue(b.files.isEmpty());
        }
    }

    @Test(timeout = 5000) public void connectionLostDuringAuthenticationIsNotReportedAsWrongCode() throws Exception {
        byte[] key = LanSync.parseKey(LanSync.newKey());
        try (ServerSocket listener = new ServerSocket(0)) {
            FutureTask<Void> server = new FutureTask<>(() -> {
                try (Socket socket = listener.accept()) {
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeInt(0x4d4f5338);
                    out.write(new byte[16]);
                    out.flush();
                    new DataInputStream(socket.getInputStream()).readFully(new byte[4 + 32 + 16]);
                    // The radio or receiver disappears without accepting or rejecting the code.
                }
                return null;
            });
            Thread thread = new Thread(server);
            thread.setDaemon(true);
            thread.start();
            try (Socket socket = new Socket("127.0.0.1", listener.getLocalPort())) {
                socket.setSoTimeout(2000);
                try {
                    LanSync.probeClient(socket.getInputStream(), socket.getOutputStream(), key);
                    fail("Une fermeture ne doit pas valider la liaison");
                } catch (IOException expected) {
                    assertFalse(expected instanceof LanSync.AuthenticationException);
                    assertTrue(expected.getMessage().contains("Liaison interrompue"));
                }
            }
            server.get();
        }
    }

    @Test public void formattedCodeAndSavedCodeHaveTheSameFingerprint() {
        String key = "0123456789abcdef0123456789abcdef";
        String formatted = "0123-4567 89AB-CDEF\n0123-4567 89AB-CDEF";
        assertEquals(key, LanSync.normalizeKey(formatted));
        assertArrayEquals(LanSync.parseKey(key), LanSync.parseKey(formatted));
        assertEquals(LanSync.keyFingerprint(key), LanSync.keyFingerprint(formatted));
        assertNotEquals(LanSync.keyFingerprint(key), LanSync.keyFingerprint("1123456789abcdef0123456789abcdef"));
        try { LanSync.parseKey("0123"); fail("Code tronqué accepté"); }
        catch (IllegalArgumentException expected) { }
    }

    @Test(timeout = 5000) public void acceptanceByteWithoutValidServerProofCannotValidatePanic() throws Exception {
        try (ServerSocket listener = new ServerSocket(0)) {
            FutureTask<Void> server = new FutureTask<>(() -> {
                try (Socket socket = listener.accept()) {
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeInt(0x4d4f5338);
                    out.write(new byte[16]);
                    out.flush();
                    new DataInputStream(socket.getInputStream()).readFully(new byte[4 + 32 + 16]);
                    out.writeByte(1);
                    out.write(new byte[32]); // Claims acceptance without knowing the shared key.
                    out.flush();
                }
                return null;
            });
            Thread thread = new Thread(server);
            thread.setDaemon(true);
            thread.start();
            try (Socket socket = new Socket("127.0.0.1", listener.getLocalPort())) {
                socket.setSoTimeout(2000);
                try {
                    LanSync.probeClient(socket.getInputStream(), socket.getOutputStream(), LanSync.parseKey(LanSync.newKey()));
                    fail("Un serveur sans preuve a validé Panic");
                } catch (LanSync.AuthenticationException expected) { }
            }
            server.get();
        }
    }

    @Test public void fiveDigitCodeDerivesTheSameKeyOnBothPeersAndCopiesBothWays() throws Exception {
        byte[] aKey = LanSync.parseKey("01234");
        byte[] bKey = LanSync.parseKey("01 234");
        // PBKDF2-HMAC-SHA1 vector independently computed for the documented wire KDF.
        assertArrayEquals(LanSync.parseKey("7e606049d9eee7d970c839717df181db"), aKey);
        assertArrayEquals(aKey, bKey);
        assertFalse(Arrays.equals(aKey, LanSync.parseKey("01235")));
        Memory a = new Memory();
        Memory b = new Memory();
        a.put("photo A");
        b.put("photo B");
        exchange(a, b, aKey, bKey);
        assertSameFiles(a, b);
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
            FutureTask<SyncResult> server = new FutureTask<>(() -> {
                try (Socket socket = listener.accept()) {
                    return LanSync.server(socket, receiverKey, new MediaRepository() {
                        @Override public List<MediaRecord> list() { throw new AssertionError("La sonde ne doit pas lire la galerie"); }
                        @Override public InputStream open(MediaRecord record) { throw new AssertionError("La sonde ne doit pas ouvrir de média"); }
                        @Override public PendingWrite begin(MediaRecord record) { throw new AssertionError("La sonde ne doit pas écrire de média"); }
                    });
                }
            });
            new Thread(server).start();
            try (Socket socket = new Socket("127.0.0.1", listener.getLocalPort())) {
                LanSync.probeClient(socket.getInputStream(), socket.getOutputStream(), receiverKey);
            }
            assertEquals(0, server.get().sent());
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
