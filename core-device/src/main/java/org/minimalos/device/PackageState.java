package org.minimalos.device;

import java.util.Objects;

public final class PackageState {
    private final String packageName;
    private final boolean installed;
    private final boolean enabled;
    private final boolean systemPackage;
    private final String versionName;

    public PackageState(String packageName, boolean installed, boolean enabled) {
        this(packageName, installed, enabled, false, "");
    }

    public PackageState(String packageName, boolean installed, boolean enabled,
            boolean systemPackage, String versionName) {
        this.packageName = Objects.requireNonNull(packageName);
        this.installed = installed;
        this.enabled = enabled;
        this.systemPackage = systemPackage;
        this.versionName = Objects.requireNonNull(versionName);
    }

    public String packageName() { return packageName; }
    public boolean installed() { return installed; }
    public boolean enabled() { return enabled; }
    public boolean systemPackage() { return systemPackage; }
    public String versionName() { return versionName; }
}
