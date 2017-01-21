package com.eaglesakura.andriders.data.notification;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.devicetest.TestUtil;
import com.eaglesakura.android.util.ImageUtil;
import com.eaglesakura.android.util.ResourceUtil;

import org.junit.Test;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileOutputStream;

public class NotificationCardTest extends AppDeviceTestCase {

    @Test
    public void VectorDrawableをBitmap化できる() throws Throwable {
        Drawable drawable = ResourceUtil.drawable(getContext(), R.drawable.ic_cycle_computer);
        assertNotNull(drawable);

        validate(drawable.getMinimumWidth()).from(1);
        validate(drawable.getMinimumHeight()).from(1);

        Bitmap image = ImageUtil.toBitmap(drawable, 72);
        assertNotNull(image);

        File png = new File(TestUtil.getCacheDirectory(getContext()), "bake.png");
        try (FileOutputStream os = new FileOutputStream(png)) {
            image.compress(Bitmap.CompressFormat.PNG, 100, os);
            AppLog.test("output -> %s", png.getAbsolutePath());
        }
    }

}