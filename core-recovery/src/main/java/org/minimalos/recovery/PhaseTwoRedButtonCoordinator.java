package org.minimalos.recovery;

public final class PhaseTwoRedButtonCoordinator implements RedButtonCoordinator {
    private final PanicPreflight preflight;

    public PhaseTwoRedButtonCoordinator(PanicPreflight preflight) {
        this.preflight = preflight;
    }

    @Override public RecoveryStatus prepareExit() {
        if (!preflight.ready()) {
            return new RecoveryStatus(false,
                    "Panic bloqué en Phase 2 : " + preflight.blockingReason()
                            + ". Aucune application ni donnée n'a été supprimée.");
        }
        return new RecoveryStatus(false,
                "Préflight satisfait, mais l'exécution destructive appartient à la Phase 4.");
    }
}
