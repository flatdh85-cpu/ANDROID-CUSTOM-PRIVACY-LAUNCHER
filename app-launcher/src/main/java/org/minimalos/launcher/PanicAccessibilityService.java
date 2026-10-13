package org.minimalos.launcher;

import android.accessibilityservice.AccessibilityService;
import android.os.*;
import android.view.accessibility.*;
import java.util.*;

/** Never clicks outside an explicitly pending Panic operation. No coordinates, global taps or stored screen text. */
public final class PanicAccessibilityService extends AccessibilityService {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable inspect = this::inspectDialog;
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        PanicAutomation.Expected expected = PanicAutomation.current();
        if (expected == null || !PanicAutomation.enabled(this) || event.getPackageName() == null
                || !expected.systemPackage.contentEquals(event.getPackageName())) return;
        handler.removeCallbacks(inspect); handler.postDelayed(inspect, 200);
    }
    private void inspectDialog() {
        PanicAutomation.Expected expected = PanicAutomation.current();
        if (expected == null || !PanicAutomation.enabled(this)) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        try {
            if (root.getPackageName() == null || !expected.systemPackage.contentEquals(root.getPackageName())) return;
            StringBuilder text = new StringBuilder(); collect(root, nodes, text, 0);
            for (AccessibilityNodeInfo node : nodes) {
                if (!node.isVisibleToUser() || !node.isEnabled() || !node.isClickable()) continue;
                String id = node.getViewIdResourceName();
                // Only conventional positive system-dialog controls; never arbitrary matching text.
                if (id == null || (!id.equals("android:id/button1") && !id.equals(expected.systemPackage + ":id/ok_button")
                        && !id.equals(expected.systemPackage + ":id/permission_allow_button"))) continue;
                if (!PanicDialogPolicy.accepts(expected.kind, expected.label, text.toString(), node.getText() == null ? "" : node.getText().toString())) continue;
                if (PanicAutomation.consume(expected)) node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return;
            }
        } finally {
            for (AccessibilityNodeInfo node : nodes) if (node != root) node.recycle();
            root.recycle();
        }
    }
    private void collect(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> nodes, StringBuilder text, int depth) {
        if (depth > 20 || nodes.size() >= 200) { node.recycle(); return; }
        nodes.add(node);
        if (node.getText() != null) text.append(node.getText()).append('\n');
        for (int i = 0; i < node.getChildCount(); i++) { AccessibilityNodeInfo child = node.getChild(i); if (child != null) collect(child, nodes, text, depth + 1); }
    }
    @Override public void onInterrupt() { PanicAutomation.clear(); handler.removeCallbacks(inspect); }
    @Override public void onDestroy() { onInterrupt(); super.onDestroy(); }
}
