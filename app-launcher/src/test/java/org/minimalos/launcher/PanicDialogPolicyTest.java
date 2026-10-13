package org.minimalos.launcher;
import org.junit.Test;
import static org.junit.Assert.*;

public class PanicDialogPolicyTest {
    @Test public void uninstallRequiresExpectedAppAndUninstallQuestion() {
        assertTrue(PanicDialogPolicy.accepts("uninstall", "SimpleX", "SimpleX : voulez-vous désinstaller cette application ?", "OK"));
        assertTrue(PanicDialogPolicy.accepts("uninstall", "LocalSend", "Uninstall LocalSend?", "Uninstall"));
        assertFalse(PanicDialogPolicy.accepts("uninstall", "SimpleX", "Uninstall Camera?", "OK"));
        assertFalse(PanicDialogPolicy.accepts("uninstall", "SimpleX", "SimpleX wants access to contacts", "OK"));
        assertFalse(PanicDialogPolicy.accepts("uninstall", "SimpleX", "Uninstall SimpleX?", "Cancel"));
    }
    @Test public void galleryDeleteRequiresMinimalOsAndDeleteAction() {
        assertTrue(PanicDialogPolicy.accepts("media-delete", "Minimal OS", "Autoriser Minimal OS à supprimer ces photos ?", "Supprimer"));
        assertFalse(PanicDialogPolicy.accepts("media-delete", "Minimal OS", "Autoriser une autre application à supprimer ces photos ?", "Supprimer"));
        assertFalse(PanicDialogPolicy.accepts("media-delete", "Minimal OS", "Minimal OS wants access to photos", "Allow"));
        assertFalse(PanicDialogPolicy.accepts("media-delete", "Minimal OS", "Minimal OS: delete these photos?", "Cancel"));
    }
    @Test public void legacyWriteGrantDoesNotAcceptGenericPermissionsOrUnknownActions() {
        assertTrue(PanicDialogPolicy.accepts("media-grant", "Minimal OS", "Allow Minimal OS to modify this photo?", "Allow"));
        assertFalse(PanicDialogPolicy.accepts("media-grant", "Minimal OS", "Allow Minimal OS to access contacts?", "Allow"));
        assertFalse(PanicDialogPolicy.accepts("unknown", "Minimal OS", "Uninstall Minimal OS?", "OK"));
    }
}
