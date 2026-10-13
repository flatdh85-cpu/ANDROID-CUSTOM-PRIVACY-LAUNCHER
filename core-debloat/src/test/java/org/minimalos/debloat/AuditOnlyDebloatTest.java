package org.minimalos.debloat;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.minimalos.device.DeviceInfo;

public class AuditOnlyDebloatTest {
    @Test public void unknownDeviceProducesNoActions() {
        DebloatPlan plan = new AuditOnlyDebloatPlanner().plan(
                new DeviceInfo("Vendor", "Model", 21, "fp", "unknown"), Collections.emptyList());
        assertFalse(plan.hasExactProfile());
        assertTrue(plan.packageActions().isEmpty());
        assertFalse(new AuditOnlyDebloatExecutor().execute(plan).changedSystem());
    }

    @Test public void executorRefusesInjectedAction() {
        DebloatPlan unsafe = new DebloatPlan(true, Arrays.asList("disable:critical.package"), "test");
        ExecutionResult result = new AuditOnlyDebloatExecutor().execute(unsafe);
        assertFalse(result.changedSystem());
        assertTrue(result.message().startsWith("Refus"));
    }
}
