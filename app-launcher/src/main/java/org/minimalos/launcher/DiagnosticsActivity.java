package org.minimalos.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import org.minimalos.device.DeviceInfo;
import org.minimalos.device.PackageState;

public final class DiagnosticsActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle(R.string.diagnostics_title);
        setContentView(buildContent());
    }

    private ScrollView buildContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Ui.color(this, R.color.background));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        scroll.addView(root, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = Ui.text(this, getString(R.string.diagnostics_title), 24, Ui.color(this, R.color.ink));
        Ui.heading(title);
        root.addView(title, Ui.matchWrap());

        DeviceInfo device = new AndroidDeviceDetector().detect();
        TextView deviceText = Ui.text(this, getString(R.string.device_format,
                device.manufacturer(), device.model(), device.sdkInt(), device.vendorVersion(),
                device.fingerprint()), 14, Ui.color(this, R.color.ink));
        root.addView(deviceText, Ui.matchWrap());

        TextView appsTitle = Ui.text(this, getString(R.string.apps_status_title), 19, Ui.color(this, R.color.ink));
        Ui.heading(appsTitle);
        LinearLayout.LayoutParams headingParams = Ui.matchWrap();
        headingParams.setMargins(0, Ui.dp(this, 20), 0, Ui.dp(this, 6));
        root.addView(appsTitle, headingParams);

        List<String> names = new ArrayList<>();
        for (AppEntry entry : AppCatalog.entries()) names.add(entry.packageName());
        List<PackageState> states = new AndroidPackageInventory(this).inspect(names);
        for (int index = 0; index < states.size(); index++) {
            AppEntry entry = AppCatalog.entries().get(index);
            PackageState packageState = states.get(index);
            int status = !packageState.installed() ? R.string.not_installed
                    : packageState.enabled() ? R.string.available : R.string.disabled;
            root.addView(Ui.text(this, "• " + entry.label() + " — " + getString(status),
                    15, Ui.color(this, R.color.ink)), Ui.matchWrap());
        }

        TextView notice = Ui.text(this, getString(R.string.audit_notice), 15, Ui.color(this, R.color.danger));
        LinearLayout.LayoutParams noticeParams = Ui.matchWrap();
        noticeParams.setMargins(0, Ui.dp(this, 20), 0, Ui.dp(this, 12));
        root.addView(notice, noticeParams);

        Button back = Ui.button(this, getString(R.string.back));
        back.setOnClickListener(view -> finish());
        root.addView(back, Ui.matchWrap());
        return scroll;
    }
}
