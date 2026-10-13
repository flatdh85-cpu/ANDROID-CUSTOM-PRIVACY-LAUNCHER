package org.minimalos.launcher;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
    private Ui() {}

    static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    @SuppressWarnings("deprecation")
    static int color(Context context, int resource) {
        return context.getResources().getColor(resource);
    }

    static TextView text(Context context, CharSequence value, float sp, int color) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setLineSpacing(0, 1.1f);
        return view;
    }

    static Button button(Context context, CharSequence value) {
        Button button = new Button(context);
        button.setText(value);
        button.setAllCaps(false);
        button.setMinHeight(dp(context, 48));
        return button;
    }

    static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    static void heading(TextView view) {
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    }

    static void margin(View view, int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams raw = view.getLayoutParams();
        if (raw instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) raw).setMargins(left, top, right, bottom);
            view.setLayoutParams(raw);
        }
    }
}
