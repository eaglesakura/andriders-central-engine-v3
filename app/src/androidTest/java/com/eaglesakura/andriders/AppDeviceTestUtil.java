package com.eaglesakura.andriders;

import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.util.IOUtil;
import com.eaglesakura.util.RandomUtil;

import android.support.annotation.NonNull;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppDeviceTestUtil {

    static File sDatabasePath;

    public static File getExternalStoragePath() {
        return sDatabasePath;
    }

    public static void onSetup(@NonNull DeviceTestCase testCase) {
        assertNotNull(testCase);
        sDatabasePath = new File(testCase.getTestContext().getExternalFilesDir(null), "test/" + RandomUtil.randShortString());
        sDatabasePath.mkdirs();
        assertTrue(sDatabasePath.isDirectory());
    }

    public static void onShutdown(@NonNull DeviceTestCase testCase) {
        assertNotNull(testCase);
        IOUtil.delete(sDatabasePath);
    }
}
