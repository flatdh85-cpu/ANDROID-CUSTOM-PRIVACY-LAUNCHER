package org.minimalos.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import java.net.InetAddress;
import org.minimalos.replication.LanSync;

final class PeerSettings {
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
    static String address(Context context) { return prefs(context).getString("address", ""); }
    static int minutes(Context context) { return prefs(context).getInt("minutes", 30); }
    static void save(Context context, String address, String code, int minutes) throws Exception {
        LanSync.parseKey(code);
        if (!address.matches("[0-9.]{7,15}")) throw new IllegalArgumentException("Adresse IPv4 invalide");
        InetAddress parsed = InetAddress.getByName(address);
        if (!(parsed instanceof java.net.Inet4Address) || !parsed.isSiteLocalAddress())
            throw new IllegalArgumentException("Adresse IPv4 invalide");
        if (minutes != 10 && minutes != 15 && minutes != 20 && minutes != 30)
            throw new IllegalArgumentException("Intervalle invalide");
        prefs(context).edit().putString("address", address).putString("code", code.toLowerCase(java.util.Locale.ROOT))
                .putInt("minutes", minutes).apply();
    }
    static void saveCode(Context context, String code) {
        LanSync.parseKey(code);
        prefs(context).edit().putString("code", code.toLowerCase(java.util.Locale.ROOT)).apply();
    }
}
