package org.minimalos.device;

import java.util.Collection;
import java.util.List;

public interface PackageInventory {
    List<PackageState> inspect(Collection<String> packageNames);
}
