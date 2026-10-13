package org.minimalos.launcher;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.system.Os;
import android.system.OsConstants;
import android.provider.MediaStore;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.minimalos.replication.MediaRecord;
import org.minimalos.replication.MediaRepository;

/** Public photos/videos plus verified private copies. Originals are never changed by sync. */
final class GalleryRepository implements MediaRepository {
    private final Context context;
    private final Map<String, Uri> galleryUris = new HashMap<>();
    private final Map<Uri, String> everyGalleryUri = new LinkedHashMap<>();
    private final Map<String, File> copies = new HashMap<>();
    GalleryRepository(Context context) { this.context = context.getApplicationContext(); }

    @Override public synchronized List<MediaRecord> list() throws IOException {
        galleryUris.clear();
        everyGalleryUri.clear();
        copies.clear();
        Map<String, MediaRecord> records = new LinkedHashMap<>();
        File directory = replicaDirectory();
        File[] existing = directory.listFiles();
        if (existing != null) for (File file : existing) {
            if (!file.getName().matches("[0-9a-f]{64}") || !file.isFile()) continue;
            String actual;
            try (InputStream input = new FileInputStream(file)) { actual = digest(input).hash; }
            if (!actual.equals(file.getName())) continue;
            MediaRecord record = new MediaRecord(actual, file.length(), actual, "received");
            records.put(actual, record);
            copies.put(actual, file);
        }
        scan(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, records);
        scan(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, records);
        return new ArrayList<>(records.values());
    }

    synchronized Map<String, Uri> galleryUris() { return new HashMap<>(galleryUris); }
    synchronized Map<Uri, String> allGalleryUris() { return new LinkedHashMap<>(everyGalleryUri); }

    private void scan(Uri collection, Map<String, MediaRecord> records) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        String[] projection = { MediaStore.MediaColumns._ID };
        try (Cursor cursor = resolver.query(collection, projection, null, null, null)) {
            if (cursor == null) throw new IOException("Galerie inaccessible");
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
            while (cursor.moveToNext()) {
                Uri item = Uri.withAppendedPath(collection, Long.toString(cursor.getLong(idColumn)));
                HashedFile hashed;
                try (InputStream stream = resolver.openInputStream(item)) {
                    if (stream == null) throw new IOException("Média inaccessible : " + item);
                    hashed = digest(stream);
                }
                String hash = hashed.hash;
                galleryUris.put(hash, item);
                everyGalleryUri.put(item, hash);
                if (!records.containsKey(hash)) records.put(hash, new MediaRecord(hash, hashed.size, hash, "gallery"));
            }
        } catch (SecurityException denied) { throw new IOException("Autorisation galerie refusée", denied); }
    }

    @Override public synchronized InputStream open(MediaRecord record) throws IOException {
        File copy = copies.get(record.id());
        if (copy != null) return new FileInputStream(copy);
        Uri uri = galleryUris.get(record.id());
        if (uri == null) throw new IOException("Média disparu : " + record.id());
        InputStream stream = context.getContentResolver().openInputStream(uri);
        if (stream == null) throw new IOException("Média inaccessible");
        return stream;
    }

    @Override public PendingWrite begin(MediaRecord record) throws IOException {
        if (!record.id().matches("[0-9a-f]{64}") || !record.id().equals(record.sha256()))
            throw new IOException("Identifiant de média invalide");
        File directory = replicaDirectory();
        File partial = new File(directory, record.id() + ".partial");
        File destination = new File(directory, record.id());
        if (destination.exists()) throw new IOException("Copie déjà présente");
        if (partial.exists()) {
            long existing = partial.length();
            long aligned = existing == record.size() ? existing : existing / 65536 * 65536;
            if (aligned > record.size()) aligned = 0;
            if (aligned == record.size()) {
                try (InputStream input = new FileInputStream(partial)) {
                    if (!digest(input).hash.equals(record.sha256())) aligned = 0;
                }
            }
            try (RandomAccessFile file = new RandomAccessFile(partial, "rw")) { file.setLength(aligned); }
        }
        long offset = partial.length();
        FileOutputStream output = new FileOutputStream(partial, true);
        return new PendingWrite() {
            private boolean finished;
            private boolean discarded;
            @Override public long resumeOffset() { return offset; }
            @Override public InputStream existing() throws IOException { return new FileInputStream(partial); }
            @Override public OutputStream output() { return output; }
            @Override public void finish(long size, String sha256) throws IOException {
                if (size != record.size() || !sha256.equals(record.sha256())) throw new IOException("Reçu incorrect");
                output.flush();
                output.getFD().sync();
                output.close();
                if (partial.length() != size) throw new IOException("Taille reçue incorrecte");
                if (!partial.renameTo(destination)) throw new IOException("Publication impossible");
                java.io.FileDescriptor directoryDescriptor = null;
                try {
                    directoryDescriptor = Os.open(directory.getAbsolutePath(), OsConstants.O_RDONLY, 0);
                    Os.fsync(directoryDescriptor);
                } catch (android.system.ErrnoException failure) {
                    destination.delete();
                    throw new IOException("Publication non synchronisée", failure);
                } finally {
                    if (directoryDescriptor != null) {
                        try { Os.close(directoryDescriptor); }
                        catch (android.system.ErrnoException ignored) { }
                    }
                }
                finished = true;
                synchronized (GalleryRepository.this) { copies.put(record.id(), destination); }
            }
            @Override public void discard() throws IOException {
                discarded = true;
                output.close();
                if (!partial.delete()) throw new IOException("Copie partielle non supprimée");
            }
            @Override public void close() throws IOException {
                output.close();
                if (!finished && !discarded) {
                    long aligned = partial.length() / 65536 * 65536;
                    if (partial.length() == record.size()) aligned = partial.length();
                    try (RandomAccessFile file = new RandomAccessFile(partial, "rw")) { file.setLength(aligned); }
                }
            }
        };
    }

    private File replicaDirectory() throws IOException {
        File directory = new File(context.getFilesDir(), "replica");
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IOException("Stockage des copies indisponible");
        return directory;
    }

    private static final class HashedFile {
        final String hash;
        final long size;
        HashedFile(String hash, long size) { this.hash = hash; this.size = size; }
    }

    private static HashedFile digest(InputStream stream) throws IOException {
        MessageDigest digest;
        try { digest = MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException impossible) { throw new IOException(impossible); }
        byte[] buffer = new byte[65536];
        int count;
        long size = 0;
        while ((count = stream.read(buffer)) != -1) { digest.update(buffer, 0, count); size += count; }
        char[] alphabet = "0123456789abcdef".toCharArray();
        byte[] bytes = digest.digest();
        char[] hex = new char[64];
        for (int i = 0; i < bytes.length; i++) { hex[i * 2] = alphabet[(bytes[i] >>> 4) & 15]; hex[i * 2 + 1] = alphabet[bytes[i] & 15]; }
        return new HashedFile(new String(hex), size);
    }
}
