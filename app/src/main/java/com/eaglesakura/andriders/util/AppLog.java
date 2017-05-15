package com.eaglesakura.andriders.util;

import com.eaglesakura.andriders.provider.LoggerProvider;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.log.Logger;
import com.eaglesakura.util.StringUtil;

import android.content.Context;

import java.lang.reflect.Method;

/**
 * アプリ用のログ出力をラップする
 */
public class AppLog {
    @Inject(value = LoggerProvider.class, name = LoggerProvider.NAME_APPLOG)
    static Logger.Impl sAppLogger;

    static Class FirebaseCrash;

    static Method FIrebaseCrash_report;

    public static void printStackTrace(Throwable e) {
        e.printStackTrace();
    }

    /**
     * send firebase report
     */
    public static void report(Throwable e) {
        e.printStackTrace();
        try {
            if (FirebaseCrash == null) {
                FirebaseCrash = Class.forName("com.google.firebase.crash.FirebaseCrash");
                FIrebaseCrash_report = FirebaseCrash.getDeclaredMethod("report", Throwable.class);
            }
            FIrebaseCrash_report.invoke(FirebaseCrash, e);
        } catch (Throwable fbc) {
            fbc.printStackTrace();
        }
    }

    /**
     * 再度ロガーを注入する
     */
    public static void inject(Context context) {
        Garnet.create(AppLog.class)
                .depend(Context.class, context)
                .inject();
    }

    public static void widget(String fmt, Object... args) {
        String tag = "App.Widget";
        sAppLogger.out(Logger.LEVEL_INFO, tag, StringUtil.format(fmt, args));
    }

    public static void system(String fmt, Object... args) {
        String tag = "App.System";
        sAppLogger.out(Logger.LEVEL_INFO, tag, StringUtil.format(fmt, args));
    }

    public static void command(String fmt, Object... args) {
        String tag = "App.Command";
        sAppLogger.out(Logger.LEVEL_INFO, tag, StringUtil.format(fmt, args));
    }

    public static void db(String fmt, Object... args) {
        String tag = "App.DB";
        sAppLogger.out(Logger.LEVEL_INFO, tag, StringUtil.format(fmt, args));
    }

    public static void gps(String fmt, Object... args) {
        String tag = "App.GPS";
        sAppLogger.out(Logger.LEVEL_INFO, tag, StringUtil.format(fmt, args));
    }

    public static void speed(String fmt, Object... args) {
        String tag = "App.Speed";
        sAppLogger.out(Logger.LEVEL_INFO, tag, StringUtil.format(fmt, args));
    }

    public static void cadence(String fmt, Object... args) {
        String tag = "App.Cadence";
        sAppLogger.out(Logger.LEVEL_INFO, tag, StringUtil.format(fmt, args));
    }

    public static void broadcast(String fmt, Object... args) {
        String tag = "App.Broadcast";
        sAppLogger.out(Logger.LEVEL_INFO, tag, StringUtil.format(fmt, args));
    }

    public static void ble(String fmt, Object... args) {
        String tag = "App.Ble";
        sAppLogger.out(Logger.LEVEL_DEBUG, tag, StringUtil.format(fmt, args));
    }

    public static void bleData(String fmt, Object... args) {
        String tag = "App.Ble.Data";
        sAppLogger.out(Logger.LEVEL_DEBUG, tag, StringUtil.format(fmt, args));
    }

    public static void proximity(String fmt, Object... args) {
        String tag = "App.Sensor.Proximity";
        sAppLogger.out(Logger.LEVEL_INFO, tag, StringUtil.format(fmt, args));
    }

    public static void plugin(String fmt, Object... args) {
        String tag = "App.Plugin";
        sAppLogger.out(Logger.LEVEL_INFO, tag, StringUtil.format(fmt, args));
    }

    public static void test(String fmt, Object... args) {
        String tag = "App.Test";
        sAppLogger.out(Logger.LEVEL_DEBUG, tag, StringUtil.format(fmt, args));
    }
}
