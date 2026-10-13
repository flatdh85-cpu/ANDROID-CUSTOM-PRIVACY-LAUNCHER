package org.minimalos.launcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Wi-Fi P2P negotiates its own link; no existing WLAN, hotspot or IP entry is used. */
@SuppressLint("MissingPermission") // Permission demandée à l'écran ; échec de job/service géré si elle est retirée.
final class WifiDirectLink {
    final WifiP2pManager manager;
    final WifiP2pManager.Channel channel;
    private WifiDirectLink(Context context) throws IOException {
        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        if (manager == null) throw new IOException("Wi-Fi Direct indisponible");
        channel = manager.initialize(context, Looper.getMainLooper(), null);
        if (channel == null) throw new IOException("Canal Wi-Fi Direct indisponible");
    }
    static WifiDirectLink create(Context context) throws IOException { return new WifiDirectLink(context); }
    void discover(WifiP2pManager.ActionListener callback) { manager.discoverPeers(channel, callback); }
    void peers(WifiP2pManager.PeerListListener callback) { manager.requestPeers(channel, callback); }
    void connect(String address, WifiP2pManager.ActionListener callback) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = address;
        config.groupOwnerIntent = 0;
        manager.connect(channel, config, callback);
    }
    void info(WifiP2pManager.ConnectionInfoListener callback) { manager.requestConnectionInfo(channel, callback); }

    /** Only the P2P group client initiates TCP. The group owner is the receiver. */
    static InetAddress awaitOwner(Context context, String peer) throws Exception {
        WifiDirectLink link = create(context);
        WifiP2pInfo current = awaitInfo(link, 2);
        if (current != null && current.groupFormed) return clientOwner(current);
        CountDownLatch discovery = new CountDownLatch(1);
        link.discover(new WifiP2pManager.ActionListener() {
            @Override public void onSuccess() { discovery.countDown(); }
            @Override public void onFailure(int reason) { discovery.countDown(); }
        });
        discovery.await(5, TimeUnit.SECONDS);
        boolean found = false;
        for (int attempt = 0; attempt < 12 && !found; attempt++) {
            CountDownLatch listed = new CountDownLatch(1);
            List<WifiP2pDevice> devices = new ArrayList<>();
            link.peers(list -> { devices.addAll(list.getDeviceList()); listed.countDown(); });
            listed.await(3, TimeUnit.SECONDS);
            for (WifiP2pDevice device : devices) if (peer.equalsIgnoreCase(device.deviceAddress)) found = true;
            if (!found) Thread.sleep(2000);
        }
        if (!found) throw new IOException("Pair Wi-Fi Direct introuvable à proximité");
        CountDownLatch requested = new CountDownLatch(1);
        link.connect(peer, new WifiP2pManager.ActionListener() {
            @Override public void onSuccess() { requested.countDown(); }
            @Override public void onFailure(int reason) { requested.countDown(); }
        });
        requested.await(5, TimeUnit.SECONDS);
        for (int attempt = 0; attempt < 30; attempt++) {
            WifiP2pInfo info = awaitInfo(link, 2);
            if (info != null && info.groupFormed) return clientOwner(info);
            Thread.sleep(1000);
        }
        throw new IOException("Connexion Wi-Fi Direct non établie");
    }
    private static WifiP2pInfo awaitInfo(WifiDirectLink link, int seconds) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        WifiP2pInfo[] value = new WifiP2pInfo[1];
        link.info(info -> { value[0] = info; latch.countDown(); });
        latch.await(seconds, TimeUnit.SECONDS);
        return value[0];
    }
    private static InetAddress clientOwner(WifiP2pInfo info) throws IOException {
        if (info.isGroupOwner) throw new IOException("Ce téléphone est chef du groupe Wi-Fi Direct : lancez la copie depuis l’autre téléphone");
        if (info.groupOwnerAddress == null) throw new IOException("Adresse Wi-Fi Direct indisponible");
        return info.groupOwnerAddress;
    }
}
