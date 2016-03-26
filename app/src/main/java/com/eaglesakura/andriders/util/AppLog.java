package com.eaglesakura.andriders.util;

import com.eaglesakura.andriders.BuildConfig;

import android.util.Log;

/**
 * アプリ用のログ出力をラップする
 */
public class AppLog {

    static final boolean stackInfo = BuildConfig.DEBUG;

    private static String wrap(String fmt, Object... args) {
        if (stackInfo) {
            StackTraceElement[] trace = new Exception().getStackTrace();
            StackTraceElement elem = trace[Math.min(trace.length - 1, 4)];
            return String.format("%s[%d] : %s", elem.getFileName(), elem.getLineNumber(), String.format(fmt, args));
        } else {
            return String.format(fmt, args);
        }
    }

    public static void gps(String fmt, Object... args) {
        Log.d("GPS", wrap(fmt, args));
    }

    public static void speed(String fmt, Object... args) {
        Log.d("Speed", wrap(fmt, args));
    }

    public static void cadence(String fmt, Object... args) {
        Log.d("Cadence", wrap(fmt, args));
    }

    public static void broadcast(String fmt, Object... args) {
        Log.d("Broadcast", wrap(fmt, args));
    }

    /**
     * BLEログ出力
     */
    public static void ble(String fmt, Object... args) {
        Log.d("BLE", wrap(fmt, args));
    }

    /**
     * BLEのデータ取得
     *
     * データ流量が多いので、基本的に切っておく
     */
    public static void bleData(String fmt, Object... args) {
        Log.d("BLE/DATA", wrap(fmt, args));
    }
}
