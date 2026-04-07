package com.example.criminalintent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Helper for loading downsampled bitmaps to avoid OOM when showing thumbnails.
 */
public class PictureUtils {

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        if (path == null || destWidth <= 0 || destHeight <= 0) {
            return null;
        }

        // Read size of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        // Figure out how much to scale down
        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            float heightScale = srcHeight / destHeight;
            float widthScale = srcWidth / destWidth;
            inSampleSize = Math.round(Math.max(heightScale, widthScale));
        }

        BitmapFactory.Options scaledOptions = new BitmapFactory.Options();
        scaledOptions.inSampleSize = Math.max(inSampleSize, 1);

        return BitmapFactory.decodeFile(path, scaledOptions);
    }
}
