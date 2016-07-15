package com.eaglesakura.andriders;

import com.eaglesakura.android.devicetest.DeviceTestCase;

import android.support.annotation.NonNull;

import static org.junit.Assert.*;

public class AppDeviceTestUtil {

    public static void onSetup(@NonNull DeviceTestCase testCase) {
        assertNotNull(testCase);
    }

    public static void onShutdown(@NonNull DeviceTestCase testCase) {
        assertNotNull(testCase);
    }
}
