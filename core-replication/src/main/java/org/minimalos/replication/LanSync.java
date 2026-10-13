package org.minimalos.replication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** Mutually authenticated, encrypted, bidirectional copy over a connected byte stream. */
public final class LanSync {
    public static final int PORT = 38742;
    private static final int MAGIC = 0x4d4f5336;
    private static final int CHUNK = 64 * 1024;
    private static final int MAX_FILES = 100000;
    private static final SecureRandom RANDOM = new SecureRandom();
    private LanSync() {}

    public static byte[] parseKey(String hex) {
        if (hex == null || !hex.matches("[0-9a-fA-F]{32}")) throw new IllegalArgumentException("Code de pair invalide");
        byte[] key = new byte[16];
        for (int i = 0; i < key.length; i++) key[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        return key;
    }

    public static String newKey() {
        byte[] key = random(16);
        return hex(key);
    }

    public static SyncResult client(Socket socket, byte[] key, MediaRepository repository) throws IOException {
        socket.setSoTimeout(15000);
        return run(socket.getInputStream(), socket.getOutputStream(), key, repository, true, socket);
    }

    public static SyncResult server(Socket socket, byte[] key, MediaRepository repository) throws IOException {
        socket.setSoTimeout(15000);
        return run(socket.getInputStream(), socket.getOutputStream(), key, repository, false, socket);
    }

    public static SyncResult client(InputStream input, OutputStream output, byte[] key,
            MediaRepository repository) throws IOException {
        return run(input, output, key, repository, true, null);
    }

    public static SyncResult server(InputStream input, OutputStream output, byte[] key,
            MediaRepository repository) throws IOException {
        return run(input, output, key, repository, false, null);
    }

    /** Authenticate a reachable receiver without transferring a manifest or media. */
    public static void probeClient(InputStream input, OutputStream output, byte[] key) throws IOException {
        if (key == null || key.length != 16) throw new IllegalArgumentException("Clé AES-128 requise");
        DataInputStream in = new DataInputStream(input);
        DataOutputStream out = new DataOutputStream(output);
        authenticateClient(in, out, key);
    }

    private static byte[][] authenticateClient(DataInputStream in, DataOutputStream out, byte[] key)
            throws IOException {
        int version;
        try { version = in.readInt(); }
        catch (IOException closed) { throw new IOException("Récepteur fermé avant authentification : vérifiez le pair sélectionné et sa réception active", closed); }
        if (version != MAGIC) throw new IOException("Versions Minimal OS différentes : installez le même APK sur les deux téléphones");
        out.writeInt(MAGIC);
        byte[] serverChallenge = readBytes(in, 16);
        out.write(auth(key, "client", serverChallenge));
        byte[] clientChallenge = random(16);
        out.write(clientChallenge);
        out.flush();
        byte[] answer;
        try { answer = readBytes(in, 32); }
        catch (IOException closed) { throw new IOException("Code partagé différent ou récepteur fermé : vérifiez le même code sur les deux téléphones", closed); }
        if (!MessageDigest.isEqual(auth(key, "server", clientChallenge), answer))
            throw new IOException("Pair non authentifié : vérifiez le code partagé");
        return new byte[][]{serverChallenge, clientChallenge};
    }

    private static SyncResult run(InputStream input, OutputStream output, byte[] key,
            MediaRepository repository, boolean client, Socket socket) throws IOException {
        if (key == null || key.length != 16) throw new IllegalArgumentException("Clé AES-128 requise");
        DataInputStream in = new DataInputStream(input);
        DataOutputStream out = new DataOutputStream(output);
        byte[] serverChallenge;
        byte[] clientChallenge;
        if (client) {
            byte[][] challenges = authenticateClient(in, out, key);
            serverChallenge = challenges[0];
            clientChallenge = challenges[1];
        } else {
            serverChallenge = random(16);
            out.writeInt(MAGIC);
            out.write(serverChallenge);
            out.flush();
            if (in.readInt() != MAGIC) throw new IOException("Versions Minimal OS différentes");
            if (!MessageDigest.isEqual(auth(key, "client", serverChallenge), readBytes(in, 32)))
                throw new IOException("Pair non authentifié");
            clientChallenge = readBytes(in, 16);
            out.write(auth(key, "server", clientChallenge));
            out.flush();
        }
        byte[] sessionKey = sessionKey(key, serverChallenge, clientChallenge);
        if (socket != null) socket.setSoTimeout(600000);
        ReplicationManifest local = null;
        String localError = null;
        try { local = new ReplicationManifest(repository.list()); }
        catch (Exception failure) { localError = failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage(); }
        String remoteError;
        if (client) {
            writeStatus(out, sessionKey, localError);
            remoteError = readStatus(in, sessionKey);
        } else {
            remoteError = readStatus(in, sessionKey);
            writeStatus(out, sessionKey, localError);
        }
        if (localError != null) throw new IOException("Galerie locale indisponible : " + localError);
        if (remoteError != null) throw new IOException("Galerie du pair indisponible : " + remoteError);
        ReplicationManifest remote;
        if (client) {
            writeManifest(out, sessionKey, local);
            remote = readManifest(in, sessionKey);
        } else {
            remote = readManifest(in, sessionKey);
            writeManifest(out, sessionKey, local);
        }
        ReplicationPlan plan = new ReplicationPlanner().compare(local, remote);
        if (client) {
            sendFiles(out, in, sessionKey, repository, plan.sendToRemote());
            receiveFiles(in, out, sessionKey, repository, plan.requestFromRemote());
        } else {
            receiveFiles(in, out, sessionKey, repository, plan.requestFromRemote());
            sendFiles(out, in, sessionKey, repository, plan.sendToRemote());
        }
        Set<String> confirmed = new HashSet<>();
        for (MediaRecord record : remote.records()) confirmed.add(record.id());
        for (MediaRecord record : plan.sendToRemote()) confirmed.add(record.id());
        return new SyncResult(plan.sendToRemote().size(), plan.requestFromRemote().size(), confirmed);
    }

    private static void writeStatus(DataOutputStream out, byte[] key, String error) throws IOException {
        byte[] message = error == null ? new byte[0] : error.getBytes(StandardCharsets.UTF_8);
        if (message.length > 512) message = copy(message, 512);
        byte[] iv = random(12);
        byte[] sealed = crypt(Cipher.ENCRYPT_MODE, key, iv, message);
        out.write(iv);
        out.writeInt(sealed.length);
        out.write(sealed);
        out.flush();
    }

    private static String readStatus(DataInputStream in, byte[] key) throws IOException {
        byte[] iv = readBytes(in, 12);
        int length = in.readInt();
        if (length < 16 || length > 528) throw new IOException("État du pair invalide");
        byte[] message = crypt(Cipher.DECRYPT_MODE, key, iv, readBytes(in, length));
        return message.length == 0 ? null : new String(message, StandardCharsets.UTF_8);
    }

    private static void writeManifest(DataOutputStream out, byte[] key, ReplicationManifest manifest) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        data.writeInt(manifest.records().size());
        for (MediaRecord record : manifest.records()) {
            if (!validWireRecord(record)) throw new IOException("Identifiant de fichier invalide");
            data.writeUTF(record.id());
            data.writeLong(record.size());
            data.writeUTF(record.sha256());
            data.writeUTF(record.originDevice());
        }
        byte[] iv = random(12);
        byte[] encrypted = crypt(Cipher.ENCRYPT_MODE, key, iv, bytes.toByteArray());
        if (encrypted.length > 16 * 1024 * 1024) throw new IOException("Manifeste trop volumineux");
        out.write(iv);
        out.writeInt(encrypted.length);
        out.write(encrypted);
        out.flush();
    }

    private static ReplicationManifest readManifest(DataInputStream in, byte[] key) throws IOException {
        byte[] iv = readBytes(in, 12);
        int length = in.readInt();
        if (length < 20 || length > 16 * 1024 * 1024) throw new IOException("Manifeste invalide");
        byte[] clear = crypt(Cipher.DECRYPT_MODE, key, iv, readBytes(in, length));
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(clear));
        int count = data.readInt();
        if (count < 0 || count > MAX_FILES) throw new IOException("Nombre de fichiers invalide");
        List<MediaRecord> records = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                MediaRecord record = new MediaRecord(data.readUTF(), data.readLong(), data.readUTF(), data.readUTF());
                if (!validWireRecord(record)) throw new IOException("Identifiant de fichier invalide");
                records.add(record);
            }
            catch (IllegalArgumentException invalid) { throw new IOException("Média invalide", invalid); }
        }
        if (data.available() != 0) throw new IOException("Manifeste avec données inattendues");
        return new ReplicationManifest(records);
    }

    private static void sendFiles(DataOutputStream out, DataInputStream in, byte[] key,
            MediaRepository repository, List<MediaRecord> records) throws IOException {
        byte[] clear = new byte[CHUNK];
        for (MediaRecord record : records) {
            out.writeUTF(record.id());
            out.flush();
            long offset = in.readLong();
            if (offset < 0 || offset > record.size() || (offset != record.size() && offset % CHUNK != 0))
                throw new IOException("Position de reprise invalide");
            byte[] ivBase = random(8);
            out.write(ivBase);
            try (InputStream source = repository.open(record)) {
                long skipped = 0;
                while (skipped < offset) {
                    int step = source.read(clear, 0, (int) Math.min(clear.length, offset - skipped));
                    if (step < 0) throw new EOFException("Source tronquée pendant la reprise");
                    skipped += step;
                }
                long remaining = record.size() - offset;
                int chunkIndex = 0;
                while (remaining > 0) {
                    int wanted = (int) Math.min(CHUNK, remaining);
                    int n = 0;
                    while (n < wanted) {
                        int step = source.read(clear, n, wanted - n);
                        if (step < 0) throw new EOFException("Source modifiée pendant le transfert");
                        n += step;
                    }
                    byte[] sealed = crypt(Cipher.ENCRYPT_MODE, key, iv(ivBase, chunkIndex++), copy(clear, n));
                    out.writeInt(sealed.length);
                    out.write(sealed);
                    remaining -= n;
                }
                if (source.read() != -1) throw new IOException("Source agrandie pendant le transfert");
            }
            out.flush();
            boolean accepted = in.readBoolean();
            if (!accepted) throw new IOException("Copie refusée par le pair : " + record.id());
            String receivedId = in.readUTF();
            long receivedSize = in.readLong();
            String receivedHash = in.readUTF();
            boolean durable = in.readBoolean();
            if (!MessageDigest.isEqual(receiptMac(key, receivedId, receivedSize, receivedHash, durable), readBytes(in, 32)))
                throw new IOException("Reçu non authentifié");
            TransferReceipt receipt = new TransferReceipt(receivedId, receivedSize, receivedHash, durable);
            if (!receipt.verifies(record)) throw new IOException("Reçu invalide : " + record.id());
        }
    }

    private static void receiveFiles(DataInputStream in, DataOutputStream out, byte[] key,
            MediaRepository repository, List<MediaRecord> expected) throws IOException {
        for (MediaRecord record : expected) {
            if (!record.id().equals(in.readUTF())) throw new IOException("Ordre des fichiers invalide");
            try (MediaRepository.PendingWrite pending = repository.begin(record)) {
                long offset = pending.resumeOffset();
                if (offset < 0 || offset > record.size() || (offset != record.size() && offset % CHUNK != 0))
                    throw new IOException("Position de reprise invalide");
                out.writeLong(offset);
                out.flush();
                byte[] base = readBytes(in, 8);
                MessageDigest digest = sha256();
                long received = 0;
                try (InputStream earlier = pending.existing()) {
                    byte[] buffer = new byte[CHUNK];
                    while (received < offset) {
                        int n = earlier.read(buffer, 0, (int) Math.min(buffer.length, offset - received));
                        if (n < 0) throw new IOException("Copie partielle raccourcie");
                        digest.update(buffer, 0, n);
                        received += n;
                    }
                }
                OutputStream target = pending.output();
                int index = 0;
                while (received < record.size()) {
                    int length = in.readInt();
                    int wanted = (int) Math.min(CHUNK, record.size() - received);
                    if (length != wanted + 16) throw new IOException("Bloc invalide");
                    byte[] clear = crypt(Cipher.DECRYPT_MODE, key, iv(base, index++), readBytes(in, length));
                    if (clear.length != wanted) throw new IOException("Taille de bloc invalide");
                    target.write(clear);
                    digest.update(clear);
                    received += clear.length;
                }
                String actual = hex(digest.digest());
                if (!actual.equals(record.sha256())) {
                    pending.discard();
                    throw new IOException("SHA-256 incorrect");
                }
                pending.finish(received, actual);
                out.writeBoolean(true);
                out.writeUTF(record.id());
                out.writeLong(received);
                out.writeUTF(actual);
                out.writeBoolean(true);
                out.write(receiptMac(key, record.id(), received, actual, true));
                out.flush();
            } catch (IOException failure) {
                try { out.writeBoolean(false); out.flush(); } catch (IOException ignored) { }
                throw failure;
            }
        }
    }

    private static byte[] copy(byte[] data, int length) {
        byte[] result = new byte[length];
        System.arraycopy(data, 0, result, 0, length);
        return result;
    }
    private static boolean validWireRecord(MediaRecord record) {
        return record.id().matches("[0-9a-f]{64}") && record.id().equals(record.sha256());
    }
    private static byte[] iv(byte[] base, int index) throws IOException {
        if (index < 0) throw new IOException("Fichier trop long");
        byte[] iv = new byte[12];
        System.arraycopy(base, 0, iv, 0, 8);
        iv[8] = (byte) (index >>> 24); iv[9] = (byte) (index >>> 16);
        iv[10] = (byte) (index >>> 8); iv[11] = (byte) index;
        return iv;
    }
    private static byte[] readBytes(DataInputStream in, int length) throws IOException {
        byte[] result = new byte[length];
        in.readFully(result);
        return result;
    }
    private static byte[] random(int length) { byte[] bytes = new byte[length]; RANDOM.nextBytes(bytes); return bytes; }
    private static String hex(byte[] bytes) {
        char[] alphabet = "0123456789abcdef".toCharArray();
        char[] result = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) { result[i * 2] = alphabet[(bytes[i] >>> 4) & 15]; result[i * 2 + 1] = alphabet[bytes[i] & 15]; }
        return new String(result);
    }
    private static MessageDigest sha256() throws IOException {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (GeneralSecurityException impossible) { throw new IOException(impossible); }
    }
    private static byte[] auth(byte[] key, String purpose, byte[] challenge) throws IOException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            mac.update(purpose.getBytes(StandardCharsets.US_ASCII));
            return mac.doFinal(challenge);
        } catch (GeneralSecurityException failure) { throw new IOException(failure); }
    }
    private static byte[] sessionKey(byte[] key, byte[] server, byte[] client) throws IOException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            mac.update("session".getBytes(StandardCharsets.US_ASCII));
            mac.update(server);
            return copy(mac.doFinal(client), 16);
        } catch (GeneralSecurityException failure) { throw new IOException(failure); }
    }
    private static byte[] receiptMac(byte[] key, String id, long size, String hash, boolean durable) throws IOException {
        return auth(key, "receipt", (id + "|" + size + "|" + hash + "|" + durable)
                .getBytes(StandardCharsets.US_ASCII));
    }
    private static byte[] crypt(int mode, byte[] key, byte[] iv, byte[] bytes) throws IOException {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(mode, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            return cipher.doFinal(bytes);
        } catch (GeneralSecurityException failure) { throw new IOException("Authentification du bloc échouée", failure); }
    }
}
