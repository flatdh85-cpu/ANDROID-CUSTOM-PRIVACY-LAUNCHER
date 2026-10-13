package org.minimalos.launcher;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import java.io.IOException;
import java.util.*;
/** Lists accessible media without reading or transferring their contents. */
final class GalleryAccess {
    static List<Uri> collections(Context context) {
        List<Uri> result = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 29) {
            for (String volume : MediaStore.getExternalVolumeNames(context)) {
                result.add(MediaStore.Images.Media.getContentUri(volume)); result.add(MediaStore.Video.Media.getContentUri(volume));
            }
        } else { result.add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); result.add(MediaStore.Video.Media.EXTERNAL_CONTENT_URI); }
        return result;
    }
    /** Lists IDs only: Panic never hashes files and never contacts another phone. Includes trashed/pending media on Android 11+. */
    @android.annotation.SuppressLint("Recycle") // Both query branches are closed by the shared try-with-resources below.
    static List<Uri> galleryUris(Context context) throws IOException {
        List<Uri> result = new ArrayList<>();
        for (Uri collection : collections(context)) {
            Cursor queried;
            if (Build.VERSION.SDK_INT >= 30) {
                android.os.Bundle options = new android.os.Bundle();
                options.putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_INCLUDE);
                options.putInt(MediaStore.QUERY_ARG_MATCH_PENDING, MediaStore.MATCH_INCLUDE);
                queried = context.getContentResolver().query(collection, new String[]{MediaStore.MediaColumns._ID}, options, null);
            } else queried = context.getContentResolver().query(collection, new String[]{MediaStore.MediaColumns._ID}, null, null, null);
            try (Cursor cursor = queried) {
                if (cursor == null) throw new IOException("Galerie inaccessible");
                while (cursor.moveToNext()) result.add(ContentUris.withAppendedId(collection, cursor.getLong(0)));
            }
        }
        return result;
    }
}
