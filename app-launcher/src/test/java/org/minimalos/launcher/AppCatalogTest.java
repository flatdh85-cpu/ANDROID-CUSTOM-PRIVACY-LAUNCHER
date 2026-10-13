package org.minimalos.launcher;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class AppCatalogTest {
    @Test public void exposesExactlySixUniquePackages() {
        assertEquals(6, AppCatalog.entries().size());
        Set<String> packages = new HashSet<>();
        for (AppEntry entry : AppCatalog.entries()) {
            assertFalse(entry.label().trim().isEmpty());
            assertFalse(entry.packageName().trim().isEmpty());
            assertTrue(entry.iconRes() != 0);
            assertTrue("duplicate package " + entry.packageName(), packages.add(entry.packageName()));
        }
    }
}
