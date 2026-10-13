package org.minimalos.device;

import java.util.Objects;

public final class PackageState {
    private final String packageName;
    private final boolean installed;
    private final boolean enabled;

    public PackageState(String packageName, boolean installed, boolean enabled) {
        this.packageName = Objects.requireNonNull(packageName);
        this.installed = installed;
        this.enabled = enabled;
    }

    public String packageName() { return packageName; }
    public boolean installed() { return installed; }
    public boolean enabled() { return enabled; }
}
