package org.minimalos.launcher;

import java.io.File;
import java.io.IOException;

/** Deletes only children of the supplied download directory; never follows symbolic links. */
final class DownloadCleaner {
    interface Cancellation { boolean stopped(); }
    static void clear(File directory, Cancellation cancellation) throws IOException {
        check(cancellation);
        if (!directory.getAbsoluteFile().equals(directory.getCanonicalFile())) throw new IOException("Dossier Téléchargements redirigé : suppression interrompue");
        if (!directory.exists()) return;
        if (!directory.isDirectory()) throw new IOException("Téléchargements n’est pas un dossier");
        File[] children = directory.listFiles();
        if (children == null) throw new IOException("Téléchargements inaccessible");
        for (File child : children) remove(child, cancellation);
        check(cancellation);
        File[] remaining = directory.listFiles();
        if (remaining == null || remaining.length != 0) throw new IOException("Des fichiers restent dans Téléchargements");
    }
    private static void remove(File file, Cancellation cancellation) throws IOException {
        check(cancellation);
        // A symlink is removed as a directory entry, not traversed (including dangling links).
        if (file.getAbsoluteFile().equals(file.getCanonicalFile()) && file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null) throw new IOException("Sous-dossier inaccessible : " + file.getName());
            for (File child : children) remove(child, cancellation);
        }
        check(cancellation);
        if (!file.delete()) throw new IOException("Fichier non supprimé : " + file.getName());
    }
    private static void check(Cancellation cancellation) throws IOException {
        if (cancellation.stopped() || Thread.currentThread().isInterrupted()) throw new IOException("Suppression interrompue");
    }
}
