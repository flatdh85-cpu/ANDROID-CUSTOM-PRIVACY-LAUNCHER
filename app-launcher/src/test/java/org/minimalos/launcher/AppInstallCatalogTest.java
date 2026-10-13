package org.minimalos.launcher;
import org.junit.Test;
import static org.junit.Assert.*;
public class AppInstallCatalogTest {
    @Test public void androidSixGetsLocalSendButNotIncompatibleApps() {
        assertEquals("localsend-arm32", AppInstallCatalog.select("org.localsend.localsend_app",23,new String[]{"armeabi-v7a"}).key);
        assertNull(AppInstallCatalog.select("chat.simplex.app",23,new String[]{"arm64-v8a"}));
        assertNull(AppInstallCatalog.select("com.bitchat.droid",23,new String[]{"arm64-v8a"}));
    }
    @Test public void followsDeviceAbiPreferenceAndRejectsUnsupportedArchitecture() {
        assertEquals("simplex-arm64",AppInstallCatalog.select("chat.simplex.app",26,new String[]{"arm64-v8a","armeabi-v7a"}).key);
        assertNull(AppInstallCatalog.select("chat.simplex.app",26,new String[]{"x86_64"}));
        assertEquals("bitchat",AppInstallCatalog.select("com.bitchat.droid",26,new String[]{"x86_64"}).key);
    }
    @Test public void everyArtifactHasPinnedIntegrityAndOfficialHttpsSource() {
        for (AppInstallCatalog.Asset asset : AppInstallCatalog.ASSETS) {
            assertTrue(asset.sha256.matches("[0-9a-f]{64}")); assertTrue(asset.certificate.matches("[0-9a-f]{64}"));
            String repository = asset.packageName.equals("chat.simplex.app") ? "simplex-chat/simplex-chat" : asset.packageName.equals("com.bitchat.droid") ? "permissionlesstech/bitchat-android" : "localsend/localsend";
            assertTrue(asset.url.startsWith("https://github.com/"+repository+"/releases/download/"));
        }
    }
}
