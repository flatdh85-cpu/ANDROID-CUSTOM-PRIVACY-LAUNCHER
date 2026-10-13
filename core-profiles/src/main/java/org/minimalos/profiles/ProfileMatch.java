package org.minimalos.profiles;

import java.util.Objects;

public final class ProfileMatch {
    private final boolean exact;
    private final String profileId;
    private final String reason;
    private final DeviceProfile profile;

    private ProfileMatch(boolean exact, String profileId, String reason, DeviceProfile profile) {
        this.exact = exact;
        this.profileId = Objects.requireNonNull(profileId);
        this.reason = Objects.requireNonNull(reason);
        this.profile = profile;
    }

    public static ProfileMatch exact(String profileId) {
        return new ProfileMatch(true, profileId, "Correspondance exacte", null);
    }

    public static ProfileMatch exact(DeviceProfile profile) {
        return new ProfileMatch(true, profile.profileId(), "Correspondance exacte", profile);
    }

    public static ProfileMatch missing(String reason) {
        return new ProfileMatch(false, "", reason, null);
    }

    public boolean isExact() { return exact; }
    public String profileId() { return profileId; }
    public String reason() { return reason; }
    public DeviceProfile profile() { return profile; }
}
