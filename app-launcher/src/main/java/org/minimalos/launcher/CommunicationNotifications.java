package org.minimalos.launcher;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** In-memory summaries only; Android owns notification storage and user access. */
final class CommunicationNotifications {
    static final class Item {
        final String packageName;
        final String title;
        final String body;
        final PendingIntent open;
        final long posted;
        Item(String packageName, String title, String body, PendingIntent open, long posted) {
            this.packageName = packageName;
            this.title = title;
            this.body = body;
            this.open = open;
            this.posted = posted;
        }
    }
    private static List<Item> items = Collections.emptyList();
    private static Runnable observer;
    private CommunicationNotifications() { }
    static synchronized List<Item> snapshot() { return new ArrayList<>(items); }
    static synchronized void observe(Runnable next) { observer = next; }
    static synchronized void clear() {
        items = Collections.emptyList();
        notifyObserver();
    }
    static synchronized void update(Context context, StatusBarNotification[] active) {
        Set<String> accepted = new HashSet<>();
        for (AppEntry entry : AppCatalog.entries()) accepted.add(AppCatalog.resolvePackageName(context, entry));
        List<Item> next = new ArrayList<>();
        if (active != null) for (StatusBarNotification status : active) {
            if (!accepted.contains(status.getPackageName())) continue;
            Notification notification = status.getNotification();
            if ((notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0) continue;
            CharSequence title = notification.extras == null ? null
                    : notification.extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence body = notification.extras == null ? null
                    : notification.extras.getCharSequence(Notification.EXTRA_TEXT);
            next.add(new Item(status.getPackageName(), clean(title), clean(body),
                    notification.contentIntent, status.getPostTime()));
        }
        Collections.sort(next, (a, b) -> Long.compare(b.posted, a.posted));
        items = next;
        notifyObserver();
    }
    private static String clean(CharSequence text) {
        if (TextUtils.isEmpty(text)) return "";
        String value = text.toString().replace('\n', ' ').trim();
        return value.length() > 160 ? value.substring(0, 160) + "…" : value;
    }
    private static void notifyObserver() { if (observer != null) observer.run(); }
    static boolean hasAccess(Context context) {
        String enabled;
        try { enabled = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners"); }
        catch (RuntimeException unavailable) { return false; }
        if (enabled == null) return false;
        ComponentName wanted = new ComponentName(context, CommunicationNotificationService.class);
        for (String item : enabled.split(":")) if (wanted.equals(ComponentName.unflattenFromString(item))) return true;
        return false;
    }
}
