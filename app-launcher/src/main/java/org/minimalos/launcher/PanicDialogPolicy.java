package org.minimalos.launcher;

import java.util.Locale;

/** Fail closed on unfamiliar wording. Called only after checking the expected system package/window. */
final class PanicDialogPolicy {
    static boolean accepts(String kind, String appLabel, String windowText, String buttonText) {
        String button = buttonText == null ? "" : buttonText.trim().toLowerCase(Locale.ROOT);
        String text = windowText == null ? "" : windowText.toLowerCase(Locale.ROOT);
        if (kind.equals("uninstall")) {
            if (appLabel == null || appLabel.trim().isEmpty() || !text.contains(appLabel.toLowerCase(Locale.ROOT))) return false;
            if (!text.contains("désinstall") && !text.contains("uninstall")) return false;
            return button.equals("désinstaller") || button.equals("uninstall") || button.equals("ok") || button.equals("oui") || button.equals("yes");
        }
        if (appLabel == null || appLabel.trim().isEmpty() || !text.contains(appLabel.toLowerCase(Locale.ROOT))) return false;
        if (kind.equals("media-delete")) {
            return (text.contains("supprim") || text.contains("delete")) && (button.equals("supprimer") || button.equals("delete"));
        }
        if (kind.equals("media-grant")) {
            return (text.contains("modifier") || text.contains("modify") || text.contains("supprim") || text.contains("delete"))
                    && (button.equals("autoriser") || button.equals("allow"));
        }
        return false;
    }
}
