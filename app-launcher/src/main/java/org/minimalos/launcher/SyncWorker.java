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
                InetAddress owner = WifiDirectLink.awaitOwner(context, peer);
                try (Socket socket = openWifi(owner)) {
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
                useful = new IOException("Liaison Bluetooth fermée : vérifiez le code et le pair sélectionné sur les deux téléphones, puis le dernier échec du récepteur", failure);
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
                    if (failure.getMessage() != null && (failure.getMessage().contains("Code partagé")
                            || failure.getMessage().contains("Versions Minimal OS"))) throw failure;
                    last = failure;
                    if (attempt < 2) Thread.sleep(1000);
                }
            }
            throw new IOException("Récepteur Bluetooth non authentifié après trois tentatives : vérifiez la V6 et la réception sur l'autre téléphone", last);
        }
        InetAddress owner = WifiDirectLink.awaitOwner(context, peer);
        try (Socket socket = openWifi(owner)) {
            socket.setSoTimeout(15000);
            LanSync.probeClient(socket.getInputStream(), socket.getOutputStream(), key);
        }
    }
    private static Socket openWifi(InetAddress owner) throws Exception {
        IOException last = null;
        for (int attempt = 0; attempt < 10; attempt++) {
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(owner, LanSync.PORT), 2000);
                return socket;
            } catch (IOException failure) {
                last = failure;
                try { socket.close(); } catch (IOException ignored) { }
                Thread.sleep(500);
            }
        }
        throw new IOException("Récepteur Wi-Fi Direct non joignable", last);
    }
}
