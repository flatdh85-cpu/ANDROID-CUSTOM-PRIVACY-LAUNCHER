package org.minimalos.launcher;

import java.util.Objects;

public final class AppEntry {
    private final String label;
    private final String packageName;
    private final int iconRes;

    public AppEntry(String label, String packageName, int iconRes) {
        this.label = Objects.requireNonNull(label);
        this.packageName = Objects.requireNonNull(packageName);
        this.iconRes = iconRes;
    }

    public String label() { return label; }
    public String packageName() { return packageName; }
    public int iconRes() { return iconRes; }
}
