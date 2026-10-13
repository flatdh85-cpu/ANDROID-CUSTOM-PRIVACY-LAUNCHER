package org.minimalos.debloat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DebloatPlan {
    private final boolean exactProfile;
    private final List<String> packageActions;
    private final String explanation;

    public DebloatPlan(boolean exactProfile, List<String> packageActions, String explanation) {
        this.exactProfile = exactProfile;
        this.packageActions = Collections.unmodifiableList(new ArrayList<>(packageActions));
        this.explanation = Objects.requireNonNull(explanation);
    }

    public boolean hasExactProfile() { return exactProfile; }
    public List<String> packageActions() { return packageActions; }
    public String explanation() { return explanation; }
}
