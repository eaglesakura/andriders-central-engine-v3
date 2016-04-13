package com.eaglesakura.andriders.provider;

import com.eaglesakura.android.garnet.BuildConfig;
import com.eaglesakura.android.garnet.Provide;
import com.eaglesakura.android.garnet.Provider;
import com.eaglesakura.util.LogUtil;

import android.util.Log;

public class LoggerProvider implements Provider {

    public static final String NAME_DEFAULT = "fw.default";

    public static final String NAME_APPLOG = "app.default";

    @Override
    public void onDependsCompleted(Object inject) {

    }

    @Override
    public void onInjectCompleted(Object inject) {

    }

    /**
     * デフォルトで使用するロガーを指定する
     */
    @Provide(name = NAME_DEFAULT)
    public LogUtil.Logger provideDefaultLogger() {
        return new LogUtil.AndroidLogger(Log.class).setStackInfo(BuildConfig.DEBUG);
    }

    @Provide(name = NAME_APPLOG)
    public LogUtil.Logger provideAppLogger() {
        return new LogUtil.AndroidLogger(Log.class) {
            @Override
            protected int getStackDepth() {
                return super.getStackDepth() + 1;
            }
        }.setStackInfo(BuildConfig.DEBUG);
    }
}
