package org.minimalos.launcher;

import android.app.Activity;
import android.app.AlertDialog;
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

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Ui.color(this, R.color.background));
        setContentView(buildContent());
    }

    @Override protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    @Override public void onBackPressed() {
        // Un launcher HOME ne doit pas se fermer vers une activité inconnue.
    }

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
        grid.setColumnCount(2);
        grid.setUseDefaultMargins(false);
        for (AppEntry entry : AppCatalog.entries()) {
            AppTile tile = new AppTile(this, entry);
            tiles.add(tile);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = Ui.dp(this, 112);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
            grid.addView(tile, params);
        }
        root.addView(grid, Ui.matchWrap());

        LinearLayout mediaRow = new LinearLayout(this);
        mediaRow.setOrientation(LinearLayout.HORIZONTAL);
        addMediaButton(mediaRow, R.string.photo, MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        addMediaButton(mediaRow, R.string.video, MediaStore.INTENT_ACTION_VIDEO_CAMERA);
        Button gallery = Ui.button(this, getString(R.string.gallery));
        gallery.setTextSize(12);
        gallery.setOnClickListener(view -> openMedia(new Intent(Intent.ACTION_VIEW,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI), R.string.gallery));
        mediaRow.addView(gallery, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(mediaRow, Ui.matchWrap());

        LinearLayout settingsRow = new LinearLayout(this);
        settingsRow.setOrientation(LinearLayout.HORIZONTAL);
        addSettingsButton(settingsRow, R.string.wifi, Settings.ACTION_WIFI_SETTINGS);
        addSettingsButton(settingsRow, R.string.bluetooth, Settings.ACTION_BLUETOOTH_SETTINGS);
        addSettingsButton(settingsRow, R.string.location, Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        root.addView(settingsRow, Ui.matchWrap());

        Button internet = Ui.button(this, getString(R.string.internet));
        internet.setOnClickListener(view -> {
            try {
                startActivity(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER));
            } catch (ActivityNotFoundException unavailable) {
                Toast.makeText(this, "Aucun navigateur installé", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(internet, Ui.matchWrap());

        Button diagnostics = Ui.button(this, getString(R.string.diagnostics));
        diagnostics.setOnClickListener(view -> startActivity(new Intent(this, DiagnosticsActivity.class)));
        LinearLayout.LayoutParams diagParams = Ui.matchWrap();
        diagParams.setMargins(0, Ui.dp(this, 8), 0, 0);
        root.addView(diagnostics, diagParams);

        Button chooseHome = Ui.button(this, getString(R.string.choose_home));
        chooseHome.setOnClickListener(view -> openSettings(Settings.ACTION_HOME_SETTINGS));
        root.addView(chooseHome, Ui.matchWrap());

        Button sync = Ui.button(this, "Copie entre téléphones");
        sync.setOnClickListener(view -> startActivity(new Intent(this, SyncActivity.class)));
        root.addView(sync, Ui.matchWrap());

        Button panic = Ui.button(this, "Panic — maintenir");
        panic.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle(R.string.panic_title)
                .setMessage("Maintenez le bouton pour ouvrir la procédure Panic et ses confirmations Android.")
                .setPositiveButton(R.string.understood, null)
                .show());
        panic.setOnLongClickListener(view -> {
            startActivity(new Intent(this, PanicActivity.class));
            return true;
        });
        root.addView(panic, Ui.matchWrap());

        return scroll;
    }

    private void addSettingsButton(LinearLayout row, int label, String action) {
        Button button = Ui.button(this, getString(label));
        button.setTextSize(12);
        button.setOnClickListener(view -> openSettings(action));
        row.addView(button, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
    }

    private void addMediaButton(LinearLayout row, int label, String action) {
        Button button = Ui.button(this, getString(label));
        button.setTextSize(12);
        button.setOnClickListener(view -> openMedia(new Intent(action), label));
        row.addView(button, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
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
            addView(icon, new LinearLayout.LayoutParams(Ui.dp(context, 40), Ui.dp(context, 40)));

            TextView label = Ui.text(context, entry.label(), 16, Ui.color(context, R.color.ink));
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
            int statusText = state == null || !state.installed() ? R.string.not_installed
                    : state.enabled() ? R.string.available : R.string.disabled;
            status.setText(statusText);
            setContentDescription(entry.label() + ", " + status.getText());
        }
    }
}
