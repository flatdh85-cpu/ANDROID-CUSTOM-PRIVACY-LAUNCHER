package org.minimalos.launcher;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;
import java.io.File;
import java.util.Arrays;

public final class ReceivedMediaActivity extends Activity {
    private LinearLayout preview;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("Copies reçues");
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 24));
        scroll.addView(root);
        root.addView(Ui.text(this, "Copies reçues de l’autre téléphone. Elles sont privées à Minimal OS sur cet appareil ; désinstaller Minimal OS les retire ici.", 16, -1));
        preview = new LinearLayout(this);
        preview.setOrientation(LinearLayout.VERTICAL);
        root.addView(preview, Ui.matchWrap());
        File directory = new File(getFilesDir(), "replica");
        File[] entries = directory.listFiles(file -> file.isFile() && file.getName().matches("[0-9a-f]{64}"));
        if (entries == null || entries.length == 0) root.addView(Ui.text(this, "Aucune copie reçue.", 16, -1));
        else {
            Arrays.sort(entries, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            for (File file : entries) {
                Button button = Ui.button(this, file.getName().substring(0, 12) + "… · " + (file.length() / 1024) + " Ko");
                button.setOnClickListener(view -> show(file));
                root.addView(button, Ui.matchWrap());
            }
        }
        setContentView(scroll);
    }

    private void show(File file) {
        preview.removeAllViews();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if (options.outWidth > 0 && options.outHeight > 0) {
            options.inJustDecodeBounds = false;
            int max = Math.max(options.outWidth, options.outHeight);
            options.inSampleSize = Math.max(1, max / 1200);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            if (bitmap == null) { preview.addView(Ui.text(this, "Image non prise en charge.", 16, -1)); return; }
            ImageView image = new ImageView(this);
            image.setImageBitmap(bitmap);
            image.setAdjustViewBounds(true);
            preview.addView(image, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 400)));
        } else {
            VideoView video = new VideoView(this);
            video.setVideoPath(file.getAbsolutePath());
            MediaController controls = new MediaController(this);
            controls.setAnchorView(video);
            video.setMediaController(controls);
            preview.addView(video, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 300)));
            video.start();
        }
    }
}
