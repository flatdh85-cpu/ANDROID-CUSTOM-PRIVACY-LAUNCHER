package org.minimalos.launcher;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class AppCatalogTest {
    @Test public void exposesOnlyRequestedApplications() {
        assertEquals(3, AppCatalog.entries().size());
        Set<String> packages = new HashSet<>();
        for (AppEntry entry : AppCatalog.entries()) {
            assertFalse(entry.label().trim().isEmpty());
            assertFalse(entry.packageName().trim().isEmpty());
            assertTrue(entry.iconRes() != 0);
            assertEquals(entry.label().equals("LocalSend") ? 21 : 26, AppCatalog.currentMinimumSdk(entry));
            assertTrue("duplicate package " + entry.packageName(), packages.add(entry.packageName()));
        }
        assertEquals(new HashSet<>(java.util.Arrays.asList("org.localsend.localsend_app", "com.bitchat.droid", "chat.simplex.app")), packages);
    }
}
