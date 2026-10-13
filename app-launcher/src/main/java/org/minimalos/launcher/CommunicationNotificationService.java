package org.minimalos.launcher;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/** Reads only notifications from the configured launcher apps, after Android grants listener access. */
public final class CommunicationNotificationService extends NotificationListenerService {
    @Override public void onListenerConnected() { refresh(); }
    @Override public void onNotificationPosted(StatusBarNotification notification) { refresh(); }
    @Override public void onNotificationRemoved(StatusBarNotification notification) { refresh(); }
    @Override public void onListenerDisconnected() { CommunicationNotifications.clear(); }
    private void refresh() {
        try { CommunicationNotifications.update(this, getActiveNotifications()); }
        catch (RuntimeException unavailable) { android.util.Log.w("MinimalOS", "Notifications indisponibles", unavailable); }
    }
}
