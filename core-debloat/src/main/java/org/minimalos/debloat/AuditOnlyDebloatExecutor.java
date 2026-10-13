package org.minimalos.debloat;

public final class AuditOnlyDebloatExecutor implements DebloatExecutor {
    @Override public ExecutionResult execute(DebloatPlan plan) {
        if (!plan.packageActions().isEmpty()) {
            return new ExecutionResult(false, "Refus : l'exécuteur audit-only n'accepte aucune action");
        }
        return new ExecutionResult(false, "Audit terminé sans modification système");
    }
}
