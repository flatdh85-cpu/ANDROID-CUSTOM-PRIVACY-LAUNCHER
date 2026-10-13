package org.minimalos.launcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import java.net.ServerSocket;
import java.net.Socket;
import org.minimalos.replication.LanSync;

/** Must be started explicitly on the peer that will receive scheduled transfers. */
public final class ReceiverService extends Service {
    private volatile boolean running;
    private ServerSocket server;
    private Thread thread;

    @Override public void onCreate() {
        super.onCreate();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26)
            manager.createNotificationChannel(new NotificationChannel("receiver", "Réception Minimal OS", NotificationManager.IMPORTANCE_LOW));
        Intent activity = new Intent(this, SyncActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, activity,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, "receiver") : new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher).setContentTitle("Minimal OS : réception active")
                .setContentText("Copie de médias depuis un appareil appairé").setOngoing(true).setContentIntent(pending);
        startForeground(42, builder.build());
        running = true;
        thread = new Thread(this::listen, "minimal-os-receiver");
        thread.start();
    }

    private void listen() {
        try (ServerSocket listener = new ServerSocket(LanSync.PORT)) {
            server = listener;
            while (running) {
                try (Socket socket = listener.accept()) {
                    LanSync.server(socket, LanSync.parseKey(PeerSettings.code(this)), new GalleryRepository(this));
                } catch (Exception failedSession) {
                    android.util.Log.w("MinimalOS", "Session de copie échouée", failedSession);
                }
            }
        } catch (Exception unavailable) {
            if (running) android.util.Log.e("MinimalOS", "Récepteur indisponible", unavailable);
        }
        stopSelf();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override public void onDestroy() {
        running = false;
        try { if (server != null) server.close(); } catch (Exception ignored) { }
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
