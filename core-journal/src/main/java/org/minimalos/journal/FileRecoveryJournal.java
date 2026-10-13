package org.minimalos.journal;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class FileRecoveryJournal implements RecoveryJournal {
    private static final int MAGIC = 0x4d4f5332;
    private static final int MAX_EVENT_BYTES = 1024 * 1024;
    private final File file;

    public FileRecoveryJournal(File file) {
        this.file = Objects.requireNonNull(file);
    }

    @Override public synchronized void append(String event) {
        byte[] payload = Objects.requireNonNull(event).getBytes(StandardCharsets.UTF_8);
        if (payload.length > MAX_EVENT_BYTES) throw new IllegalArgumentException("Événement trop volumineux");
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Impossible de créer le dossier du journal");
        }
        try (FileOutputStream stream = new FileOutputStream(file, true);
             DataOutputStream output = new DataOutputStream(stream)) {
            output.writeInt(MAGIC);
            output.writeInt(payload.length);
            output.write(payload);
            output.write(digest(payload));
            output.flush();
            stream.getFD().sync();
        } catch (IOException failure) {
            throw new IllegalStateException("Impossible d'écrire le journal", failure);
        }
    }

    @Override public synchronized List<String> events() {
        if (!file.exists()) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            while (true) {
                int magic;
                try {
                    magic = input.readInt();
                } catch (EOFException end) {
                    break;
                }
                if (magic != MAGIC) throw new IllegalStateException("Journal corrompu : en-tête invalide");
                int length = input.readInt();
                if (length < 0 || length > MAX_EVENT_BYTES) {
                    throw new IllegalStateException("Journal corrompu : longueur invalide");
                }
                byte[] payload = new byte[length];
                byte[] expected = new byte[32];
                try {
                    input.readFully(payload);
                    input.readFully(expected);
                } catch (EOFException interruptedWrite) {
                    break;
                }
                if (!MessageDigest.isEqual(expected, digest(payload))) {
                    throw new IllegalStateException("Journal corrompu : empreinte invalide");
                }
                result.add(new String(payload, StandardCharsets.UTF_8));
            }
            return Collections.unmodifiableList(result);
        } catch (IOException failure) {
            throw new IllegalStateException("Impossible de lire le journal", failure);
        }
    }

    private static byte[] digest(byte[] payload) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(payload);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 indisponible", impossible);
        }
    }
}
