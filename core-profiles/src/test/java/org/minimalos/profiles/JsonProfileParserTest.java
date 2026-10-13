package org.minimalos.profiles;

import static org.junit.Assert.*;
import java.util.Collections;
import org.junit.Test;
import org.minimalos.device.DeviceInfo;

public class JsonProfileParserTest {
    private static final String VALID = "{"
            + "\"schemaVersion\":1,"
            + "\"profileId\":\"verified-device\","
            + "\"manufacturer\":\"Vendor\","
            + "\"model\":\"Model\","
            + "\"sdkInt\":29,"
            + "\"vendorVersion\":\"ROM-1\","
            + "\"fingerprint\":\"vendor/model/build\","
            + "\"packages\":[{"
            + "\"packageName\":\"com.vendor.optional\","
            + "\"classification\":\"candidate\","
            + "\"justification\":\"Test physique validé\","
            + "\"prerequisites\":[\"launcher natif actif\"],"
            + "\"risk\":\"low\","
            + "\"method\":\"disable-user\","
            + "\"restoreStrategy\":\"pm enable\","
            + "\"source\":\"inventaire appareil\","
            + "\"validation\":\"firmware exact\","
            + "\"tests\":[\"appel\",\"SMS\"]}]}";

    @Test public void parsesCompleteVersionedProfile() {
        DeviceProfile profile = new JsonProfileParser().parse(VALID);
        assertEquals("verified-device", profile.profileId());
        assertEquals(PackageClassification.CANDIDATE, profile.packages().get(0).classification());
        assertEquals(2, profile.packages().get(0).tests().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnknownSchema() {
        new JsonProfileParser().parse(VALID.replace("\"schemaVersion\":1", "\"schemaVersion\":2"));
    }

    @Test public void requiresExactFingerprintAndVendorVersion() {
        DeviceProfile profile = new JsonProfileParser().parse(VALID);
        StrictProfileRepository repository = new StrictProfileRepository(Collections.singletonList(profile));
        assertTrue(repository.findExact(new DeviceInfo("vendor", "model", 29,
                "vendor/model/build", "ROM-1")).isExact());
        assertFalse(repository.findExact(new DeviceInfo("vendor", "model", 29,
                "vendor/model/other", "ROM-1")).isExact());
        assertFalse(repository.findExact(new DeviceInfo("vendor", "model", 29,
                "vendor/model/build", "ROM-2")).isExact());
    }
}
