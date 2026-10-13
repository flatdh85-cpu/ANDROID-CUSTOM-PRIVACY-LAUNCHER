package org.minimalos.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import org.minimalos.replication.LanSync;

final class PeerSettings {
    static final String BLUETOOTH = "bluetooth";
    static final String WIFI_DIRECT = "wifi_direct";
    private static final String PREFS = "peer-v1";
    static SharedPreferences prefs(Context context) { return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }
    static String code(Context context) {
        SharedPreferences prefs = prefs(context);
        String code = prefs.getString("code", "");
        if (code.isEmpty()) {
            code = LanSync.newKey();
            prefs.edit().putString("code", code).apply();
        }
        return code;
    }
    static String transport(Context context) { return prefs(context).getString("transport", BLUETOOTH); }
    static String peer(Context context) { return prefs(context).getString("radio_peer", ""); }
    static int minutes(Context context) { return prefs(context).getInt("minutes", 30); }
    static void save(Context context, String transport, String peer, String code, int minutes) {
        LanSync.parseKey(code);
        if (!BLUETOOTH.equals(transport) && !WIFI_DIRECT.equals(transport))
            throw new IllegalArgumentException("Transport inconnu");
        if (!peer.matches("(?i)[0-9a-f]{2}(:[0-9a-f]{2}){5}"))
            throw new IllegalArgumentException("Choisissez un appareil découvert ou appairé");
        if (minutes != 10 && minutes != 15 && minutes != 20 && minutes != 30)
            throw new IllegalArgumentException("Intervalle invalide");
        prefs(context).edit().putString("transport", transport).putString("radio_peer", peer)
                .putString("code", code.toLowerCase(java.util.Locale.ROOT)).putInt("minutes", minutes).apply();
    }
    static void saveCode(Context context, String code) {
        LanSync.parseKey(code);
        prefs(context).edit().putString("code", code.toLowerCase(java.util.Locale.ROOT)).apply();
    }
}
