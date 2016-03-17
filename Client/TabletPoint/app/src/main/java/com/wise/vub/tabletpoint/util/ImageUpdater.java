package com.wise.vub.tabletpoint.util;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by Chaun on 3/14/2016.
 */
public class ImageUpdater {

    // Util function to update image with resource passed in.
    public static Bitmap updateImageChange (Bitmap image, String frameInfo) {
        String[] values;
        int x, y, rgb, count;
        try {
            for (String rgbs : frameInfo.split(Constants.object_separator.getName())) {
                Log.d(Constants.tag.getName(), rgbs);
                values = rgbs.split(Constants.value_seperator.getName());
                x = Integer.valueOf(values[0]);
                y = Integer.valueOf(values[1]);
                rgb = Integer.valueOf(values[2]);
                if (values.length == 3) {
                    image.setPixel(x, y, rgb);
                }
            }
        } catch (Exception e) {
            Log.d(Constants.tag.getName(), "Image Update Exception");
        }
        return image;
    }


    public static Bitmap updateImageChange(Bitmap image) {
        try {
            for (int x = 0;x < image.getWidth();x++) {
                for (int y = 0;y < image.getHeight();y++) {
                    image.setPixel(x, y, 255);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.tag.getName(), "What?", e);
        }
        return image;
    }
}
