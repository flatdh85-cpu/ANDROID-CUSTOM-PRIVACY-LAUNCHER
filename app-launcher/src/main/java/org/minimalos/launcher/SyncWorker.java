package org.minimalos.launcher;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.minimalos.replication.LanSync;
import org.minimalos.replication.SyncResult;

final class SyncWorker {
    static synchronized SyncResult sync(Context context) throws Exception {
        try {
            String peer = PeerSettings.peer(context);
            if (peer.isEmpty()) throw new IllegalStateException("Choisissez un appareil pair");
            byte[] key = LanSync.parseKey(PeerSettings.code(context));
            SyncResult result;
            if (PeerSettings.BLUETOOTH.equals(PeerSettings.transport(context))) {
                try (BluetoothSocket socket = BluetoothLink.connect(peer)) {
                    result = LanSync.client(socket.getInputStream(), socket.getOutputStream(), key,
                            new GalleryRepository(context));
                }
            } else {
                try (WifiDirectLink.Connection connection = WifiDirectLink.awaitOwner(context, peer);
                        Socket socket = openWifi(connection.owner)) {
                    result = LanSync.client(socket, key, new GalleryRepository(context));
                }
            }
            PeerSettings.prefs(context).edit().putLong("last_success_ms", System.currentTimeMillis())
                    .putInt("last_sent", result.sent()).putInt("last_received", result.received())
                    .remove("last_error").apply();
            return result;
        } catch (Exception failure) {
            Exception useful = failure;
            String message = failure.getMessage();
            if (PeerSettings.BLUETOOTH.equals(PeerSettings.transport(context)) && message != null
                    && (message.contains("read failed") || message.contains("socket might closed")))
                useful = new IOException("Liaison Bluetooth interrompue pendant l'échange : consultez l'état du récepteur sur l'autre téléphone", failure);
            PeerSettings.prefs(context).edit().putString("last_error", useful.getMessage()).apply();
            throw useful;
        }
    }
    static synchronized void probe(Context context) throws Exception {
        String peer = PeerSettings.peer(context);
        if (peer.isEmpty()) throw new IllegalStateException("Choisissez un appareil pair");
        byte[] key = LanSync.parseKey(PeerSettings.code(context));
        if (PeerSettings.BLUETOOTH.equals(PeerSettings.transport(context))) {
            IOException last = null;
            for (int attempt = 0; attempt < 3; attempt++) {
                try (BluetoothSocket socket = BluetoothLink.connect(peer)) {
                    LanSync.probeClient(socket.getInputStream(), socket.getOutputStream(), key);
                    return;
                } catch (IOException failure) {
                    if (failure instanceof LanSync.AuthenticationException || failure instanceof LanSync.VersionException) throw failure;
                    last = failure;
                    if (attempt < 2) Thread.sleep(1000);
                }
            }
            throw new IOException("Bluetooth : trois tentatives échouées. " + (last == null ? "Réception indisponible" : last.getMessage()), last);
        }
        try (WifiDirectLink.Connection connection = WifiDirectLink.awaitOwner(context, peer);
                Socket socket = openWifi(connection.owner)) {
            socket.setSoTimeout(15000);
            LanSync.probeClient(socket.getInputStream(), socket.getOutputStream(), key);
        }
    }
    private static Socket openWifi(InetAddress owner) throws Exception {
        IOException last = null;
        for (int attempt = 0; attempt < 15; attempt++) {
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(owner, LanSync.PORT), 2000);
                return socket;
            } catch (IOException failure) {
                last = failure;
                try { socket.close(); } catch (IOException ignored) { }
                if (attempt < 14) Thread.sleep(1000);
            }
        }
        String detail = last == null ? "Pas de réponse" : last.getClass().getSimpleName() + ": " + last.getMessage();
        throw new IOException("Groupe Wi-Fi Direct connecté, mais réception inaccessible sur "
                + owner.getHostAddress() + ":" + LanSync.PORT + ". Vérifiez que l'autre téléphone affiche « Wi-Fi Direct : en écoute ». "
                + detail, last);
    }
}
