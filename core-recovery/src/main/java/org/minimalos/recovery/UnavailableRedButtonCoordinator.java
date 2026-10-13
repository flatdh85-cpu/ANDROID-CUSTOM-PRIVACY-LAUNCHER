package org.minimalos.recovery;

public final class UnavailableRedButtonCoordinator implements RedButtonCoordinator {
    @Override public RecoveryStatus prepareExit() {
        return new RecoveryStatus(false,
                "Phase 1 : utilisez le sélecteur HOME Android ; aucune désinstallation n'est effectuée");
    }
}
