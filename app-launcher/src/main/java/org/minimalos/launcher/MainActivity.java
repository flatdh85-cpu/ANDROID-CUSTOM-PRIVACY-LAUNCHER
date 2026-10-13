package org.minimalos.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.minimalos.device.PackageState;

public final class MainActivity extends Activity {
    private final List<AppTile> tiles = new ArrayList<>();
    private TextView systemStatus;
    private LinearLayout notificationList;
    private Button notificationAccess;
    private final android.os.Handler panicHold = new android.os.Handler(android.os.Looper.getMainLooper());
    private long panicDown;
    private final Runnable openPanic = () -> startActivity(new Intent(this, PanicActivity.class).putExtra("execute", true));

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Ui.color(this, R.color.background));
        BootReceiver.retireTransfers(this);
        setContentView(buildContent());
        if (state == null && !getSharedPreferences("setup", MODE_PRIVATE).getBoolean("shown-v9", false)) {
            getSharedPreferences("setup", MODE_PRIVATE).edit().putBoolean("shown-v9", true).apply();
            startActivity(new Intent(this, AppInstallerActivity.class));
        }
    }

    @Override protected void onResume() {
        super.onResume();
        CommunicationNotifications.observe(() -> runOnUiThread(this::refreshNotifications));
        refreshStatus();
        refreshNotifications();
    }

    @Override protected void onPause() {
        panicHold.removeCallbacks(openPanic);
        CommunicationNotifications.observe(null);
        super.onPause();
    }

    @Override public void onBackPressed() {
        // Un launcher HOME ne doit pas se fermer vers une activité inconnue.
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility") // Touch listener only cancels the timer; Button retains its normal click/long-click accessibility actions.
    private View buildContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Ui.color(this, R.color.background));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 24));
        scroll.addView(root, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextClock clock = new TextClock(this);
        clock.setFormat24Hour("HH:mm");
        clock.setFormat12Hour("h:mm");
        clock.setTextSize(42);
        clock.setTextColor(Ui.color(this, R.color.ink));
        clock.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        clock.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(clock, Ui.matchWrap());

        TextView subtitle = Ui.text(this, getString(R.string.home_subtitle), 16, Ui.color(this, R.color.muted));
        subtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(subtitle, Ui.matchWrap());

        systemStatus = Ui.text(this, "", 15, Ui.color(this, R.color.ink));
        systemStatus.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams statusParams = Ui.matchWrap();
        statusParams.setMargins(0, Ui.dp(this, 8), 0, Ui.dp(this, 12));
        root.addView(systemStatus, statusParams);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        grid.setUseDefaultMargins(false);
        for (AppEntry entry : AppCatalog.entries()) {
            AppTile tile = new AppTile(this, entry);
            tiles.add(tile);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = Ui.dp(this, 128);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
            grid.addView(tile, params);
        }
        root.addView(grid, Ui.matchWrap());

        root.addView(Ui.text(this, "Notifications de communication", 17, Ui.color(this, R.color.ink)), Ui.matchWrap());
        notificationAccess = Ui.button(this, "Activer les notifications dans Android");
        notificationAccess.setOnClickListener(view -> openSettings(android.os.Build.VERSION.SDK_INT >= 22
                ? Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS : Settings.ACTION_SETTINGS));
        root.addView(notificationAccess, Ui.matchWrap());
        notificationList = new LinearLayout(this);
        notificationList.setOrientation(LinearLayout.VERTICAL);
        root.addView(notificationList, Ui.matchWrap());

        LinearLayout mediaRow = new LinearLayout(this);
        addAction(mediaRow, "Photo", R.drawable.ic_photo, () -> openMedia(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA), R.string.photo));
        addAction(mediaRow, "Vidéo", R.drawable.ic_video, () -> openMedia(new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA), R.string.video));
        addAction(mediaRow, "Galerie", R.drawable.ic_gallery, () -> openMedia(new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), R.string.gallery));
        root.addView(mediaRow, Ui.matchWrap());
        LinearLayout settingsRow = new LinearLayout(this);
        addAction(settingsRow, getString(R.string.wifi), R.drawable.ic_wifi, () -> openSettings(Settings.ACTION_WIFI_SETTINGS));
        addAction(settingsRow, getString(R.string.bluetooth), R.drawable.ic_bluetooth, () -> openSettings(Settings.ACTION_BLUETOOTH_SETTINGS));
        addAction(settingsRow, getString(R.string.location), R.drawable.ic_location, () -> openSettings(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        root.addView(settingsRow, Ui.matchWrap());
        LinearLayout internetRow = new LinearLayout(this);
        addAction(internetRow, getString(R.string.internet), R.drawable.ic_internet, () -> {
            try { startActivity(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)); }
            catch (ActivityNotFoundException unavailable) { Toast.makeText(this, "Aucun navigateur installé", Toast.LENGTH_SHORT).show(); }
        });
        root.addView(internetRow, Ui.matchWrap());

        Button installer = Ui.button(this, "Applications et autorisations");
        installer.setOnClickListener(view -> startActivity(new Intent(this, AppInstallerActivity.class)));
        root.addView(installer, Ui.matchWrap());

        Button diagnostics = Ui.button(this, getString(R.string.diagnostics));
        diagnostics.setOnClickListener(view -> startActivity(new Intent(this, DiagnosticsActivity.class)));
        LinearLayout.LayoutParams diagParams = Ui.matchWrap();
        diagParams.setMargins(0, Ui.dp(this, 8), 0, 0);
        root.addView(diagnostics, diagParams);

        Button chooseHome = Ui.button(this, getString(R.string.choose_home));
        chooseHome.setOnClickListener(view -> openSettings(Settings.ACTION_HOME_SETTINGS));
        root.addView(chooseHome, Ui.matchWrap());

        Button panic = Ui.button(this, "Panic — maintenir 3 secondes");
        android.graphics.drawable.GradientDrawable panicBackground = new android.graphics.drawable.GradientDrawable();
        panicBackground.setColor(android.graphics.Color.WHITE); panicBackground.setCornerRadius(Ui.dp(this, 20));
        panic.setBackground(panicBackground); panic.setTextColor(android.graphics.Color.BLACK);
        panic.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        panic.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle(R.string.panic_title)
                .setMessage(PanicAutomation.enabled(this) ? "L’auto-validation est activée. Maintenez Panic pour supprimer les trois applications, la galerie, les téléchargements et Minimal OS sans sauvegarde." : "Maintenez le bouton pour ouvrir Panic. Vous pourrez y configurer l’auto-validation des confirmations Android.")
                .setPositiveButton(R.string.understood, null)
                .show());
        panic.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) panicDown = android.os.SystemClock.elapsedRealtime();
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP || event.getActionMasked() == android.view.MotionEvent.ACTION_CANCEL
                    || event.getX() < 0 || event.getX() > view.getWidth() || event.getY() < 0 || event.getY() > view.getHeight()) {
                panicHold.removeCallbacks(openPanic); panicDown = 0;
            }
            return false;
        });
        panic.setOnLongClickListener(view -> {
            long elapsed = panicDown == 0 ? 0 : android.os.SystemClock.elapsedRealtime() - panicDown;
            panicHold.postDelayed(openPanic, Math.max(0, 3000 - elapsed));
            return true;
        });
        root.addView(panic, Ui.matchWrap());

        return scroll;
    }

    private void addAction(LinearLayout row, String title, int drawable, Runnable action) {
        LinearLayout tile = new LinearLayout(this); tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER); tile.setBackgroundResource(R.drawable.tile_background);
        tile.setFocusable(true); tile.setClickable(true); tile.setContentDescription(title);
        ImageView icon = new ImageView(this); icon.setImageResource(drawable);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        tile.addView(icon, new LinearLayout.LayoutParams(Ui.dp(this, 26), Ui.dp(this, 26)));
        TextView label = Ui.text(this, title, 13, Ui.color(this, R.color.ink)); label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams labelParams = Ui.matchWrap(); labelParams.topMargin = Ui.dp(this, 8); tile.addView(label, labelParams);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, Ui.dp(this, 86), 1);
        int gap = Ui.dp(this, 4); params.setMargins(gap, gap, gap, gap); row.addView(tile, params);
        tile.setOnClickListener(view -> action.run());
    }

    private void openMedia(Intent intent, int label) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException unavailable) {
            Toast.makeText(this, getString(R.string.media_unavailable, getString(label)),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void openSettings(String action) {
        try {
            startActivity(new Intent(action));
        } catch (RuntimeException unavailable) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    private void refreshStatus() {
        int battery = batteryPercent();
        String batteryText = battery >= 0 ? getString(R.string.battery_format, battery)
                : getString(R.string.battery_unknown);
        systemStatus.setText(getString(R.string.system_status_format, batteryText,
                getString(isConnected() ? R.string.network_connected : R.string.network_disconnected)));

        List<String> names = new ArrayList<>();
        for (AppEntry entry : AppCatalog.entries()) names.add(AppCatalog.resolvePackageName(this, entry));
        List<PackageState> states = new AndroidPackageInventory(this).inspect(names);
        Map<String, PackageState> byName = new HashMap<>();
        for (PackageState item : states) byName.put(item.packageName(), item);
        for (AppTile tile : tiles) tile.setPackageState(byName.get(AppCatalog.resolvePackageName(this, tile.entry)));
    }

    private void refreshNotifications() {
        if (notificationList == null) return;
        boolean access = CommunicationNotifications.hasAccess(this);
        notificationAccess.setVisibility(access ? View.GONE : View.VISIBLE);
        notificationList.removeAllViews();
        if (!access) {
            notificationList.addView(Ui.text(this, "Accordez une fois l’accès aux notifications à Minimal OS pour afficher les messages ici et sur les raccourcis.", 13,
                    Ui.color(this, R.color.muted)), Ui.matchWrap());
        }
        List<CommunicationNotifications.Item> items = access
                ? CommunicationNotifications.snapshot() : new ArrayList<>();
        Map<String, Integer> counts = new HashMap<>();
        for (CommunicationNotifications.Item item : items) {
            Integer previous = counts.get(item.packageName);
            counts.put(item.packageName, previous == null ? 1 : previous + 1);
        }
        for (AppTile tile : tiles) {
            Integer count = counts.get(AppCatalog.resolvePackageName(this, tile.entry));
            tile.setBadge(count == null ? 0 : count);
        }
        if (access && items.isEmpty())
            notificationList.addView(Ui.text(this, "Aucune notification de ces applications pour le moment.", 13,
                    Ui.color(this, R.color.muted)), Ui.matchWrap());
        for (int i = 0; i < Math.min(8, items.size()); i++) {
            CommunicationNotifications.Item item = items.get(i);
            String label = item.packageName;
            for (AppEntry entry : AppCatalog.entries())
                if (AppCatalog.resolvePackageName(this, entry).equals(item.packageName)) { label = entry.label(); break; }
            String detail = item.title.isEmpty() ? label : label + " · " + item.title;
            if (!item.body.isEmpty()) detail += "\n" + item.body;
            Button row = Ui.button(this, detail);
            row.setOnClickListener(view -> {
                try {
                    if (item.open != null) item.open.send();
                    else openNotificationApp(item.packageName);
                } catch (PendingIntent.CanceledException expired) { openNotificationApp(item.packageName); }
            });
            notificationList.addView(row, Ui.matchWrap());
        }
    }

    private void openNotificationApp(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) startActivity(intent);
    }

    private int batteryPercent() {
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery == null) return -1;
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return level >= 0 && scale > 0 ? Math.round(level * 100f / scale) : -1;
    }

    private boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo network = manager == null ? null : manager.getActiveNetworkInfo();
        return network != null && network.isConnected();
    }

    private void launch(AppEntry entry, PackageState state) {
        if (state == null || !state.installed()) {
            if (android.os.Build.VERSION.SDK_INT < AppCatalog.currentMinimumSdk(entry)) {
                new AlertDialog.Builder(this).setTitle(entry.label())
                        .setMessage("La version actuelle de cette application exige Android 8 ou plus. Minimal OS fonctionne sur Android 6, mais ne peut pas rendre une application tierce incompatible installable.")
                        .setPositiveButton(R.string.understood, null).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.missing_title, entry.label()))
                    .setMessage(R.string.missing_message)
                    .setPositiveButton(R.string.open_app_settings,
                            (dialog, which) -> openSettings(Settings.ACTION_APPLICATION_SETTINGS))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return;
        }
        Intent intent = getPackageManager().getLaunchIntentForPackage(state.packageName());
        if (intent == null || !state.enabled()) {
            Toast.makeText(this, getString(R.string.cannot_open, entry.label()), Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
    }

    private final class AppTile extends LinearLayout {
        private final AppEntry entry;
        private final TextView status;
        private PackageState packageState;
        private int badge;

        AppTile(Context context, AppEntry entry) {
            super(context);
            this.entry = entry;
            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER);
            setPadding(Ui.dp(context, 8), Ui.dp(context, 8), Ui.dp(context, 8), Ui.dp(context, 8));
            setBackgroundResource(R.drawable.tile_background);
            setFocusable(true);
            setClickable(true);
            setContentDescription(entry.label());

            ImageView icon = new ImageView(context);
            icon.setImageResource(entry.iconRes());
            icon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(icon, new LinearLayout.LayoutParams(Ui.dp(context, 32), Ui.dp(context, 32)));

            TextView label = Ui.text(context, entry.label(), 14, Ui.color(context, R.color.ink));
            label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            label.setGravity(Gravity.CENTER);
            addView(label, Ui.matchWrap());

            status = Ui.text(context, getString(R.string.not_installed), 12, Ui.color(context, R.color.muted));
            status.setGravity(Gravity.CENTER);
            addView(status, Ui.matchWrap());
            setOnClickListener(view -> launch(entry, packageState));
        }

        void setPackageState(PackageState state) {
            packageState = state;
            updateStatus();
        }

        void setBadge(int count) {
            badge = count;
            updateStatus();
        }

        private void updateStatus() {
            PackageState state = packageState;
            int statusText = state == null || !state.installed() ? R.string.not_installed
                    : state.enabled() ? R.string.available : R.string.disabled;
            if ((state == null || !state.installed()) && android.os.Build.VERSION.SDK_INT < AppCatalog.currentMinimumSdk(entry)) {
                status.setText(R.string.requires_android_eight); setContentDescription(entry.label() + ", " + status.getText()); return;
            }
            status.setText(badge > 0 ? getResources().getQuantityString(R.plurals.tile_status_notifications,
                    badge, getString(statusText), badge) : getString(statusText));
            setContentDescription(entry.label() + ", " + status.getText());
        }
    }
}
