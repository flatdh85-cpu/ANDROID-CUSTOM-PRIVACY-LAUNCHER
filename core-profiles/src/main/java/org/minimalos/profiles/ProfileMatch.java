package org.minimalos.profiles;

import java.util.Objects;

public final class ProfileMatch {
    private final boolean exact;
    private final String profileId;
    private final String reason;

    private ProfileMatch(boolean exact, String profileId, String reason) {
        this.exact = exact;
        this.profileId = Objects.requireNonNull(profileId);
        this.reason = Objects.requireNonNull(reason);
    }

    public static ProfileMatch exact(String profileId) {
        return new ProfileMatch(true, profileId, "Correspondance exacte");
    }

    public static ProfileMatch missing(String reason) {
        return new ProfileMatch(false, "", reason);
    }

    public boolean isExact() { return exact; }
    public String profileId() { return profileId; }
    public String reason() { return reason; }
}
