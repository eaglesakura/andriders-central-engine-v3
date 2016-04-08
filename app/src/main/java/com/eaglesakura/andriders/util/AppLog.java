package com.eaglesakura.andriders.util;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.util.EnvironmentUtil;
import com.eaglesakura.util.LogUtil;

import android.util.Log;

/**
 * アプリ用のログ出力をラップする
 */
public class AppLog {
    private static final LogUtil.Logger sAppLogger;

    static {
        if (EnvironmentUtil.isRunningRobolectric()) {
            sAppLogger = ((level, tag, msg) -> {
                switch (level) {
                    case LogUtil.LOGGER_LEVEL_INFO:
                        tag = "I/" + tag;
                        break;
                    case LogUtil.LOGGER_LEVEL_ERROR:
                        tag = "E/" + tag;
                        break;
                    default:
                        tag = "D/" + tag;
                        break;
                }

                try {
                    StackTraceElement[] trace = new Exception().getStackTrace();
                    StackTraceElement elem = trace[Math.min(trace.length - 1, 3)];
                    System.out.println(String.format("%s | %s[%d] : %s", tag, elem.getFileName(), elem.getLineNumber(), msg));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            sAppLogger = new LogUtil.AndroidLogger(Log.class) {
                @Override
                protected int getStackDepth() {
                    return super.getStackDepth() + 1;
                }
            }.setStackInfo(BuildConfig.DEBUG);
        }
    }

    static final boolean stackInfo = BuildConfig.DEBUG;

    private static String wrap(String fmt, Object... args) {
        if (stackInfo) {
            StackTraceElement[] trace = new Exception().getStackTrace();
            StackTraceElement elem = trace[Math.min(trace.length - 1, 2)];
            return String.format("%s[%d] : %s", elem.getFileName(), elem.getLineNumber(), String.format(fmt, args));
        } else {
            return String.format(fmt, args);
        }
    }

    public static void widget(String fmt, Object... args) {
        String tag = "App.Widget";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void system(String fmt, Object... args) {
        String tag = "App.System";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void db(String fmt, Object... args) {
        String tag = "App.DB";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void gps(String fmt, Object... args) {
        String tag = "App.GPS";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void speed(String fmt, Object... args) {
        String tag = "App.Speed";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void cadence(String fmt, Object... args) {
        String tag = "App.Cadence";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void broadcast(String fmt, Object... args) {
        String tag = "App.broadcast";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void ble(String fmt, Object... args) {
        String tag = "App.Ble";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void bleData(String fmt, Object... args) {
        String tag = "App.Ble.Data";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void test(String fmt, Object... args) {
        String tag = "App.Test";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }
}
