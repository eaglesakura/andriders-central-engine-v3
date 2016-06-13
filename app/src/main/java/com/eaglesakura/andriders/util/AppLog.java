package com.eaglesakura.andriders.util;

import com.eaglesakura.andriders.provider.LoggerProvider;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.util.LogUtil;

import android.content.Context;

/**
 * アプリ用のログ出力をラップする
 */
public class AppLog {

    @Inject(value = LoggerProvider.class, name = LoggerProvider.NAME_DEFAULT)
    static LogUtil.Logger sDefaultLogger;

    @Inject(value = LoggerProvider.class, name = LoggerProvider.NAME_APPLOG)
    static LogUtil.Logger sAppLogger;

    /**
     * 再度ロガーを注入する
     */
    public static void inject(Context context) {
        Garnet.create(AppLog.class)
                .depend(Context.class, context)
                .inject();
        LogUtil.setLogger(sDefaultLogger);
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
    public static void plugin(String fmt, Object... args) {
        String tag = "App.Plugin";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void test(String fmt, Object... args) {
        String tag = "App.Test";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }
}
