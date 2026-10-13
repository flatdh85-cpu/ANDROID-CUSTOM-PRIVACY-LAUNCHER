package org.minimalos.replication;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import org.junit.Test;

public class ReplicationPlannerTest {
    private static MediaRecord media(String id, String origin) {
        return new MediaRecord(id, 100,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", origin);
    }

    @Test public void plansCumulativeBidirectionalCopy() {
        ReplicationManifest a = new ReplicationManifest(Arrays.asList(media("A1", "A"), media("A2", "A")));
        ReplicationManifest b = new ReplicationManifest(Arrays.asList(media("B1", "B"), media("B2", "B")));
        ReplicationPlan plan = new ReplicationPlanner().compare(a, b);
        assertEquals(2, plan.sendToRemote().size());
        assertEquals(2, plan.requestFromRemote().size());
    }

    @Test public void localDeletionRequestsMissingCopyInsteadOfPropagatingDeletion() {
        MediaRecord a1 = media("A1", "A");
        ReplicationManifest localAfterDeletion = new ReplicationManifest(Collections.emptyList());
        ReplicationManifest remoteStillHasCopy = new ReplicationManifest(Collections.singletonList(a1));
        ReplicationPlan plan = new ReplicationPlanner().compare(localAfterDeletion, remoteStillHasCopy);
        assertTrue(plan.sendToRemote().isEmpty());
        assertEquals("A1", plan.requestFromRemote().get(0).id());
    }

    @Test public void receiptRequiresDurableSizeAndHashMatch() {
        MediaRecord expected = media("A1", "A");
        String hash = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        assertTrue(new TransferReceipt("A1", 100, hash, true).verifies(expected));
        assertFalse(new TransferReceipt("A1", 99, hash, true).verifies(expected));
        assertFalse(new TransferReceipt("A1", 100, hash, false).verifies(expected));
    }

    @Test public void videoNeverFallsBackToBluetoothOnly() {
        TransportSelector selector = new TransportSelector();
        assertEquals(Transport.NONE, selector.select(
                EnumSet.of(Transport.BLUETOOTH_CLASSIC), 50L * 1024 * 1024));
        assertEquals(Transport.WIFI_DIRECT, selector.select(
                EnumSet.of(Transport.BLUETOOTH_CLASSIC, Transport.WIFI_DIRECT),
                50L * 1024 * 1024));
    }
}
