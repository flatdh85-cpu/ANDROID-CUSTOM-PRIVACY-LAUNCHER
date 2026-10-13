package org.minimalos.recovery;

public final class PanicPreflight {
    private final boolean nativeLauncherAvailable;
    private final boolean journalValid;
    private final boolean installProvenanceComplete;
    private final boolean mediaReplicationVerified;

    public PanicPreflight(boolean nativeLauncherAvailable, boolean journalValid,
            boolean installProvenanceComplete, boolean mediaReplicationVerified) {
        this.nativeLauncherAvailable = nativeLauncherAvailable;
        this.journalValid = journalValid;
        this.installProvenanceComplete = installProvenanceComplete;
        this.mediaReplicationVerified = mediaReplicationVerified;
    }

    public boolean ready() {
        return nativeLauncherAvailable && journalValid && installProvenanceComplete
                && mediaReplicationVerified;
    }

    public String blockingReason() {
        if (!nativeLauncherAvailable) return "Launcher natif non vérifié";
        if (!journalValid) return "Journal de récupération non vérifié";
        if (!installProvenanceComplete) return "Provenance des applications incomplète";
        if (!mediaReplicationVerified) return "Réplication des médias non confirmée";
        return "Préflight complet";
    }
}
