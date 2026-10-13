package org.minimalos.launcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AppCatalog {
    private AppCatalog() {}

    public static List<AppEntry> entries() {
        return Collections.unmodifiableList(Arrays.asList(
                new AppEntry("Telegram", "org.telegram.messenger", R.drawable.ic_telegram),
                new AppEntry("Canta", "org.samo_lego.canta", R.drawable.ic_canta),
                new AppEntry("Shizuku", "moe.shizuku.privileged.api", R.drawable.ic_shizuku),
                new AppEntry("SimpleX Chat", "chat.simplex.app", R.drawable.ic_simplex),
                new AppEntry("Briar", "org.briarproject.briar.android", R.drawable.ic_briar),
                new AppEntry("LocalSend", "org.localsend.localsend_app", R.drawable.ic_localsend)
        ));
    }
}
