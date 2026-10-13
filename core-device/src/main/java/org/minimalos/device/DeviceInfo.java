package org.minimalos.device;

import java.util.Objects;

public final class DeviceInfo {
    private final String manufacturer;
    private final String model;
    private final int sdkInt;
    private final String fingerprint;
    private final String vendorVersion;

    public DeviceInfo(String manufacturer, String model, int sdkInt, String fingerprint, String vendorVersion) {
        this.manufacturer = Objects.requireNonNull(manufacturer);
        this.model = Objects.requireNonNull(model);
        this.sdkInt = sdkInt;
        this.fingerprint = Objects.requireNonNull(fingerprint);
        this.vendorVersion = Objects.requireNonNull(vendorVersion);
    }

    public String manufacturer() { return manufacturer; }
    public String model() { return model; }
    public int sdkInt() { return sdkInt; }
    public String fingerprint() { return fingerprint; }
    public String vendorVersion() { return vendorVersion; }
}
