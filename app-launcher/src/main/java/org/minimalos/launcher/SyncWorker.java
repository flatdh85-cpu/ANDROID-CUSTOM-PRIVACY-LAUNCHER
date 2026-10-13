package org.minimalos.launcher;

import android.content.Context;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collections;
import org.minimalos.replication.LanSync;
import org.minimalos.replication.SyncResult;

final class SyncWorker {
    static SyncResult sync(Context context) throws Exception {
        try {
            String address = PeerSettings.address(context);
            if (address.isEmpty()) throw new IllegalStateException("Adresse du pair absente");
            InetAddress remote = InetAddress.getByName(address);
            for (NetworkInterface network : Collections.list(NetworkInterface.getNetworkInterfaces()))
                for (InetAddress local : Collections.list(network.getInetAddresses()))
                    if (remote.equals(local)) throw new IllegalStateException("Le pair désigne ce téléphone");
            byte[] key = LanSync.parseKey(PeerSettings.code(context));
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(address, LanSync.PORT), 15000);
                SyncResult result = LanSync.client(socket, key, new GalleryRepository(context));
                PeerSettings.prefs(context).edit().putLong("last_success_ms", System.currentTimeMillis())
                        .putInt("last_sent", result.sent()).putInt("last_received", result.received())
                        .remove("last_error").apply();
                return result;
            }
        } catch (Exception failure) {
            PeerSettings.prefs(context).edit().putString("last_error", failure.getMessage()).apply();
            throw failure;
        }
    }
}
