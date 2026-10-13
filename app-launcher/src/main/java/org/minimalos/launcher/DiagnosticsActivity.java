package org.minimalos.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import org.minimalos.debloat.CriticalPackagePolicy;
import org.minimalos.debloat.DebloatPlan;
import org.minimalos.debloat.ProfileAwareDebloatPlanner;
import org.minimalos.device.DeviceInfo;
import org.minimalos.device.PackageState;
import org.minimalos.journal.FileRecoveryJournal;
import org.minimalos.profiles.EmptyProfileRepository;
import org.minimalos.profiles.ProfileMatch;
import org.minimalos.profiles.ProfileRepository;

public final class DiagnosticsActivity extends Activity {
    private TextView inventoryStatus;
    private TextView profileStatus;
    private FileRecoveryJournal journal;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle(R.string.diagnostics_title);
        journal = new FileRecoveryJournal(new java.io.File(getFilesDir(), "recovery/audit-v2.bin"));
        setContentView(buildContent());
        runFullAudit();
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

        inventoryStatus = Ui.text(this, getString(R.string.inventory_pending), 15,
                Ui.color(this, R.color.muted));
        LinearLayout.LayoutParams inventoryParams = Ui.matchWrap();
        inventoryParams.setMargins(0, Ui.dp(this, 16), 0, Ui.dp(this, 4));
        root.addView(inventoryStatus, inventoryParams);

        profileStatus = Ui.text(this, getString(R.string.profile_format,
                getString(R.string.inventory_pending)), 15, Ui.color(this, R.color.muted));
        root.addView(profileStatus, Ui.matchWrap());

        TextView appsTitle = Ui.text(this, getString(R.string.apps_status_title), 19, Ui.color(this, R.color.ink));
        Ui.heading(appsTitle);
        LinearLayout.LayoutParams headingParams = Ui.matchWrap();
        headingParams.setMargins(0, Ui.dp(this, 20), 0, Ui.dp(this, 6));
        root.addView(appsTitle, headingParams);

        List<String> names = new ArrayList<>();
        for (AppEntry entry : AppCatalog.entries()) names.add(AppCatalog.resolvePackageName(this, entry));
        List<PackageState> states = new AndroidPackageInventory(this).inspect(names);
        for (int index = 0; index < states.size(); index++) {
            AppEntry entry = AppCatalog.entries().get(index);
            PackageState packageState = states.get(index);
            int status = !packageState.installed() ? R.string.not_installed
                    : packageState.enabled() ? R.string.available : R.string.disabled;
            root.addView(Ui.text(this, getString(R.string.app_status_format,
                            entry.label(), getString(status)),
                    15, Ui.color(this, R.color.ink)), Ui.matchWrap());
        }

        TextView notice = Ui.text(this, getString(R.string.audit_notice), 15, Ui.color(this, R.color.danger));
        LinearLayout.LayoutParams noticeParams = Ui.matchWrap();
        noticeParams.setMargins(0, Ui.dp(this, 20), 0, Ui.dp(this, 12));
        root.addView(notice, noticeParams);

        Button export = Ui.button(this, getString(R.string.export_journal));
        export.setOnClickListener(view -> exportJournal());
        root.addView(export, Ui.matchWrap());

        Button back = Ui.button(this, getString(R.string.back));
        back.setOnClickListener(view -> finish());
        root.addView(back, Ui.matchWrap());
        return scroll;
    }

    private void runFullAudit() {
        new Thread(() -> {
            try {
                DeviceInfo device = new AndroidDeviceDetector().detect();
                List<PackageState> packages = new AndroidPackageInventory(this).inspectAll();
                int systemCount = 0;
                for (PackageState item : packages) if (item.systemPackage()) systemCount++;

                ProfileRepository profiles = new EmptyProfileRepository();
                ProfileMatch match = profiles.findExact(device);
                DebloatPlan plan = new ProfileAwareDebloatPlanner(profiles,
                        new CriticalPackagePolicy()).plan(device, packages);
                StringBuilder snapshot = new StringBuilder();
                snapshot.append(System.currentTimeMillis())
                        .append("|AUDIT|manufacturer=").append(safe(device.manufacturer()))
                        .append("|model=").append(safe(device.model()))
                        .append("|sdk=").append(device.sdkInt())
                        .append("|fingerprint=").append(safe(device.fingerprint()))
                        .append("|packages=").append(packages.size())
                        .append("|exactProfile=").append(match.isExact())
                        .append("|plannedActions=").append(plan.packageActions().size());
                for (PackageState item : packages) {
                    snapshot.append('\n').append("PACKAGE|name=").append(safe(item.packageName()))
                            .append("|enabled=").append(item.enabled())
                            .append("|system=").append(item.systemPackage())
                            .append("|version=").append(safe(item.versionName()));
                }
                journal.append(snapshot.toString());

                int finalSystemCount = systemCount;
                runOnUiThread(() -> {
                    String visible = getResources().getQuantityString(R.plurals.inventory_visible,
                            packages.size(), packages.size());
                    String system = getResources().getQuantityString(R.plurals.inventory_system,
                            finalSystemCount, finalSystemCount);
                    inventoryStatus.setText(getString(R.string.inventory_format, visible, system));
                    profileStatus.setText(getString(R.string.profile_format, match.reason()));
                });
            } catch (RuntimeException failure) {
                runOnUiThread(() -> inventoryStatus.setText(getString(R.string.audit_failed,
                        failure.getClass().getSimpleName())));
            }
        }, "minimal-os-audit").start();
    }

    private void exportJournal() {
        String text;
        try {
            text = journal.exportText();
        } catch (RuntimeException failure) {
            inventoryStatus.setText(getString(R.string.audit_failed,
                    failure.getClass().getSimpleName()));
            return;
        }
        if (text.isEmpty()) {
            inventoryStatus.setText(R.string.journal_empty);
            return;
        }
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_journal_title));
        share.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(share, getString(R.string.export_journal_title)));
    }

    private static String safe(String value) {
        return value.replace('|', '_').replace('\n', '_').replace('\r', '_');
    }
}
