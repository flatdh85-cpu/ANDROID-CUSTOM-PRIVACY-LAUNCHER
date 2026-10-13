package org.minimalos.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public final class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                || !PeerSettings.prefs(context).getBoolean("receiver_enabled", false)) return;
        try {
            Intent service = new Intent(context, ReceiverService.class);
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service);
            else context.startService(service);
        } catch (RuntimeException blocked) {
            android.util.Log.w("MinimalOS", "Redémarrage du récepteur refusé par Android", blocked);
        }
    }
}
