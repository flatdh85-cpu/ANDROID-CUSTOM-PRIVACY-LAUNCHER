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
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private Button notificationPreview;
    private final android.os.Handler panicHold = new android.os.Handler(android.os.Looper.getMainLooper());
    private long panicDown;
    private boolean homeVisible;
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
        homeVisible=true;
        CommunicationNotifications.observe(() -> runOnUiThread(this::refreshNotifications));
        refreshStatus();
        refreshNotifications();
    }

    @Override protected void onPause() {
        homeVisible=false;
        panicHold.removeCallbacks(openPanic);
        CommunicationNotifications.observe(null);
        super.onPause();
    }

    @Override public void onBackPressed() {
        // Un launcher HOME ne doit pas se fermer vers une activité inconnue.
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility") // Touch listener only cancels the timer; Button retains its normal click/long-click accessibility actions.
    private View buildContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setFitsSystemWindows(true);
        root.setBackgroundColor(Ui.color(this,R.color.background));
        int padding=Ui.dp(this,6); root.setPadding(padding,padding,padding,padding);

        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout clockColumn = new LinearLayout(this); clockColumn.setOrientation(LinearLayout.VERTICAL);
        TextClock clock = new TextClock(this); clock.setFormat24Hour("HH:mm"); clock.setFormat12Hour("h:mm");
        clock.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,Ui.dp(this,24)); clock.setTextColor(Ui.color(this,R.color.ink)); clock.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        clock.setSingleLine(true); clockColumn.addView(clock,Ui.matchWrap());
        systemStatus=Ui.text(this,"",10,Ui.color(this,R.color.muted)); systemStatus.setSingleLine(true); systemStatus.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,Ui.dp(this,10));
        systemStatus.setEllipsize(android.text.TextUtils.TruncateAt.END); clockColumn.addView(systemStatus,Ui.matchWrap());
        header.addView(clockColumn,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1));
        notificationPreview=Ui.button(this,"Notifications"); notificationPreview.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,Math.min(12*getResources().getDisplayMetrics().scaledDensity,Ui.dp(this,14)));
        notificationPreview.setMaxLines(2); notificationPreview.setEllipsize(android.text.TextUtils.TruncateAt.END);
        notificationPreview.setPadding(Ui.dp(this,8),0,Ui.dp(this,8),0);
        notificationPreview.setOnClickListener(view -> showNotifications());
        header.addView(notificationPreview,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,2));
        root.addView(header,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,Ui.dp(this,54)));

        HomeGrid grid=new HomeGrid(this);
        for (AppEntry entry:AppCatalog.entries()) { AppTile tile=new AppTile(this,entry); tiles.add(tile); grid.addView(tile); }
        addAction(grid,"Photo",R.drawable.ic_photo,() -> openMedia(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),R.string.photo));
        addAction(grid,"Vidéo",R.drawable.ic_video,() -> openMedia(new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA),R.string.video));
        addAction(grid,"Galerie",R.drawable.ic_gallery,() -> openMedia(new Intent(Intent.ACTION_VIEW,MediaStore.Images.Media.EXTERNAL_CONTENT_URI),R.string.gallery));
        addAction(grid,getString(R.string.wifi),R.drawable.ic_wifi,() -> openSettings(Settings.ACTION_WIFI_SETTINGS));
        addAction(grid,getString(R.string.bluetooth),R.drawable.ic_bluetooth,() -> openSettings(Settings.ACTION_BLUETOOTH_SETTINGS));
        addAction(grid,getString(R.string.location),R.drawable.ic_location,() -> openSettings(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        addAction(grid,getString(R.string.internet),R.drawable.ic_internet,() -> {
            try { startActivity(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN,Intent.CATEGORY_APP_BROWSER)); }
            catch (ActivityNotFoundException unavailable) { Toast.makeText(this,"Aucun navigateur installé",Toast.LENGTH_SHORT).show(); }
        });
        addAction(grid,"Thème sombre",R.drawable.ic_night,this::openDarkTheme);
        addAction(grid,"Applications",R.drawable.ic_apps,() -> startActivity(new Intent(this,AppInstallerActivity.class)));
        addAction(grid,getString(R.string.diagnostics),R.drawable.ic_diagnostics,() -> startActivity(new Intent(this,DiagnosticsActivity.class)));
        addAction(grid,getString(R.string.choose_home),R.drawable.ic_home,() -> openSettings(Settings.ACTION_HOME_SETTINGS));
        root.addView(grid,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));

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
        panic.setTextSize(14); panic.setMaxLines(1); panic.setEllipsize(android.text.TextUtils.TruncateAt.END);
        root.addView(panic,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,Ui.dp(this,48)));
        return root;
    }

    private void addAction(HomeGrid grid,String title,int drawable,Runnable action) {
        HomeTile tile=new HomeTile(this,title,drawable,false);
        tile.setOnClickListener(view -> action.run()); grid.addView(tile);
    }

    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused);
        if (focused) getWindow().getDecorView().post(this::offerSystemDarkTheme);
    }
    private void offerSystemDarkTheme() {
        if (!homeVisible || !hasWindowFocus() || isFinishing()) return;
        Intent home=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        android.content.pm.ResolveInfo resolved=getPackageManager().resolveActivity(home,android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
        if (resolved==null || resolved.activityInfo==null || !getPackageName().equals(resolved.activityInfo.packageName)) return;
        android.content.SharedPreferences prefs=getSharedPreferences("appearance",MODE_PRIVATE);
        if (prefs.getBoolean("dark-theme-offered",false)) return;
        prefs.edit().putBoolean("dark-theme-offered",true).apply();
        int night=getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (night==android.content.res.Configuration.UI_MODE_NIGHT_YES) return;
        new AlertDialog.Builder(this).setTitle("Thème sombre du téléphone")
                .setMessage(android.os.Build.VERSION.SDK_INT>=29
                        ? "Minimal OS est votre accueil par défaut. Activez le thème sombre dans Android pour que le téléphone et les applications compatibles l’utilisent. Certaines applications ont leur propre réglage."
                        : "Minimal OS reste sombre. Sur cette version d’Android, un thème sombre général dépend du constructeur ; les applications peuvent aussi avoir leur propre réglage.")
                .setPositiveButton("Ouvrir le réglage",(dialog,which) -> openDarkTheme())
                .setNegativeButton("Plus tard",null).show();
    }
    private void openDarkTheme() {
        try { startActivity(new Intent("android.settings.DARK_THEME_SETTINGS")); }
        catch (RuntimeException unavailable) { openSettings(Settings.ACTION_DISPLAY_SETTINGS); }
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

    @android.annotation.SuppressLint("SetTextI18n")
    private void refreshNotifications() {
        if (notificationPreview==null) return;
        boolean access=CommunicationNotifications.hasAccess(this);
        List<CommunicationNotifications.Item> items=access?CommunicationNotifications.snapshot():new ArrayList<>();
        Map<String,Integer> counts=new HashMap<>();
        for (CommunicationNotifications.Item item:items) {
            Integer count=counts.get(item.packageName); counts.put(item.packageName,count==null?1:count+1);
        }
        for (AppTile tile:tiles) {
            Integer count=counts.get(AppCatalog.resolvePackageName(this,tile.entry)); tile.setBadge(count==null?0:count);
        }
        if (!access) notificationPreview.setText("Activer les notifications");
        else if (items.isEmpty()) notificationPreview.setText("Aucune notification");
        else {
            CommunicationNotifications.Item first=items.get(0);
            notificationPreview.setText(items.size()+" notification(s)\n"+(first.title.isEmpty()?first.body:first.title));
        }
    }
    private void showNotifications() {
        if (!CommunicationNotifications.hasAccess(this)) {
            openSettings(android.os.Build.VERSION.SDK_INT>=22?Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS:Settings.ACTION_SETTINGS); return;
        }
        List<CommunicationNotifications.Item> items=CommunicationNotifications.snapshot();
        if (items.isEmpty()) { Toast.makeText(this,"Aucune notification de communication",Toast.LENGTH_SHORT).show(); return; }
        String[] labels=new String[items.size()];
        for(int i=0;i<items.size();i++) {
            CommunicationNotifications.Item item=items.get(i); String name=item.packageName;
            for(AppEntry entry:AppCatalog.entries()) if(AppCatalog.resolvePackageName(this,entry).equals(item.packageName)) { name=entry.label(); break; }
            labels[i]=name+" · "+item.title+(item.body.isEmpty()?"":"\n"+item.body);
        }
        new AlertDialog.Builder(this).setTitle("Notifications de communication").setItems(labels,(dialog,which) -> {
            CommunicationNotifications.Item item=items.get(which);
            try { if(item.open!=null) item.open.send(); else openNotificationApp(item.packageName); }
            catch(PendingIntent.CanceledException expired) { openNotificationApp(item.packageName); }
        }).setNegativeButton("Fermer",null).show();
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

    private class HomeTile extends LinearLayout {
        final ImageView icon;
        final TextView label,status;
        final boolean withStatus;
        HomeTile(Context context,String title,int drawable,boolean withStatus) {
            super(context); this.withStatus=withStatus;
            setOrientation(VERTICAL); setGravity(Gravity.CENTER);
            int pad=Ui.dp(context,4); setPadding(pad,pad,pad,pad);
            setBackgroundResource(R.drawable.tile_background); setFocusable(true); setClickable(true); setContentDescription(title);
            icon=new ImageView(context); icon.setImageResource(drawable); icon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(icon,new LinearLayout.LayoutParams(Ui.dp(context,28),Ui.dp(context,28)));
            label=Ui.text(context,title,14,Ui.color(context,R.color.ink)); label.setGravity(Gravity.CENTER); label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            label.setIncludeFontPadding(false); label.setSingleLine(true); label.setEllipsize(android.text.TextUtils.TruncateAt.END); addView(label,Ui.matchWrap());
            status=Ui.text(context,"",11,Ui.color(context,R.color.muted)); status.setGravity(Gravity.CENTER);
            status.setIncludeFontPadding(false); status.setSingleLine(true); status.setEllipsize(android.text.TextUtils.TruncateAt.END); status.setVisibility(withStatus?VISIBLE:GONE); addView(status,Ui.matchWrap());
        }
        @Override protected void onMeasure(int widthSpec,int heightSpec) {
            int height=View.MeasureSpec.getSize(heightSpec);
            boolean showIcon=height>=Ui.dp(getContext(),withStatus?68:52);
            icon.setVisibility(showIcon?VISIBLE:GONE);
            int iconSize=Ui.dp(getContext(),height>=Ui.dp(getContext(),90)?30:20);
            icon.getLayoutParams().height=iconSize; icon.getLayoutParams().width=iconSize;
            float width=Math.max(1,View.MeasureSpec.getSize(widthSpec)-getPaddingLeft()-getPaddingRight());
            float textHeight=Math.max(1,height-getPaddingTop()-getPaddingBottom()-(showIcon?iconSize:0));
            float maxFont=textHeight/(withStatus?2.5f:1.3f);
            fitText(label,14,width,maxFont); fitText(status,11,width,maxFont);
            super.onMeasure(widthSpec,heightSpec);
        }
        private void fitText(TextView view,float sp,float width,float maxFont) {
            float preferred=Math.min(maxFont,sp*getResources().getDisplayMetrics().scaledDensity);
            android.graphics.Paint measuring=new android.graphics.Paint(view.getPaint()); measuring.setTextSize(preferred);
            float measured=measuring.measureText(view.getText().toString());
            float floor=10*getResources().getDisplayMetrics().density;
            view.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,Math.min(preferred,Math.max(floor,preferred*width/Math.max(1,measured))));
        }
    }

    private final class AppTile extends HomeTile {
        private final AppEntry entry;
        private PackageState packageState;
        private int badge;
        AppTile(Context context,AppEntry entry) {
            super(context,entry.label(),entry.iconRes(),true); this.entry=entry;
            setOnClickListener(view -> launch(entry,packageState));
        }

        void setPackageState(PackageState state) {
            packageState = state;
            updateStatus();
        }

        void setBadge(int count) {
            badge = count;
            updateStatus();
        }

        @android.annotation.SuppressLint("SetTextI18n")
        private void updateStatus() {
            PackageState state = packageState;
            int statusText = state == null || !state.installed() ? R.string.not_installed
                    : state.enabled() ? R.string.available : R.string.disabled;
            if ((state == null || !state.installed()) && android.os.Build.VERSION.SDK_INT < AppCatalog.currentMinimumSdk(entry)) {
                status.setText(R.string.requires_android_eight); setContentDescription(entry.label() + ", " + status.getText()); return;
            }
            status.setText(badge>0 ? badge+" notif." : getString(statusText));
            setContentDescription(entry.label()+", "+getString(statusText)+(badge>0?", "+badge+" notifications":""));
        }
    }
}
