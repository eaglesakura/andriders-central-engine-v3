package com.eaglesakura.andriders.ble;

import android.util.Log;

public class SensorUtil {

    /**
     * センサーが停止したと判断するタイムアウト時間
     */
    public static final int SENSOR_TIMEOUT_MS = 1000 * 5;

    public static void i(String msg) {
        Log.d("BLE", msg);
    }

    public static void d(String msg) {
        Log.d("BLE", msg);
    }

    public static void d(Exception e) {
        e.printStackTrace();
    }
}
