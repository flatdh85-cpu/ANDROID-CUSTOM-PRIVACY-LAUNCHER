package org.minimalos.launcher;
import java.io.*;
import java.nio.file.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
public class DownloadCleanerTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    @Test public void deletesAllTypesAndNestedFoldersButKeepsRootAndOutsideFiles() throws Exception {
        File root = temporary.newFolder("Download"), outside = temporary.newFile("keep.txt");
        Files.write(new File(root, "app.apk").toPath(), new byte[]{1});
        File nested = new File(root, "nested"); assertTrue(nested.mkdir());
        Files.write(new File(nested, ".document.pdf").toPath(), new byte[]{2});
        DownloadCleaner.clear(root, () -> false);
        assertTrue(root.isDirectory()); assertEquals(0, root.list().length); assertTrue(outside.exists());
    }
    @Test public void neverTraversesSymlinkToAnotherDirectory() throws Exception {
        File root = temporary.newFolder("Download"), outside = temporary.newFolder("outside");
        File keep = new File(outside, "keep.apk"); assertTrue(keep.createNewFile());
        Files.createSymbolicLink(new File(root, "shortcut").toPath(), outside.toPath());
        DownloadCleaner.clear(root, () -> false);
        assertTrue(keep.exists()); assertEquals(0, root.list().length);
    }
    @Test public void redirectedRootIsRejected() throws Exception {
        File outside = temporary.newFolder("outside"); File keep = new File(outside, "keep"); assertTrue(keep.createNewFile());
        File root = new File(temporary.getRoot(), "Download"); Files.createSymbolicLink(root.toPath(), outside.toPath());
        try { DownloadCleaner.clear(root, () -> false); fail(); } catch (IOException expected) { }
        assertTrue(keep.exists());
    }
    @Test public void cancellationLeavesFilesAndReportsFailure() throws Exception {
        File root = temporary.newFolder("Download"); File keep = new File(root, "keep.apk"); assertTrue(keep.createNewFile());
        try { DownloadCleaner.clear(root, () -> true); fail(); } catch (IOException expected) { }
        assertTrue(keep.exists());
    }
}
