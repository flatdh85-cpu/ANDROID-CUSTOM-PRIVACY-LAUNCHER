package org.minimalos.profiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DeviceProfile {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    private final int schemaVersion;
    private final String profileId;
    private final String manufacturer;
    private final String model;
    private final int sdkInt;
    private final String vendorVersion;
    private final String fingerprint;
    private final List<ProfilePackageRule> packages;

    public DeviceProfile(int schemaVersion, String profileId, String manufacturer, String model,
            int sdkInt, String vendorVersion, String fingerprint, List<ProfilePackageRule> packages) {
        this.schemaVersion = schemaVersion;
        this.profileId = Objects.requireNonNull(profileId);
        this.manufacturer = Objects.requireNonNull(manufacturer);
        this.model = Objects.requireNonNull(model);
        this.sdkInt = sdkInt;
        this.vendorVersion = Objects.requireNonNull(vendorVersion);
        this.fingerprint = Objects.requireNonNull(fingerprint);
        this.packages = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(packages)));
    }

    public int schemaVersion() { return schemaVersion; }
    public String profileId() { return profileId; }
    public String manufacturer() { return manufacturer; }
    public String model() { return model; }
    public int sdkInt() { return sdkInt; }
    public String vendorVersion() { return vendorVersion; }
    public String fingerprint() { return fingerprint; }
    public List<ProfilePackageRule> packages() { return packages; }
}
