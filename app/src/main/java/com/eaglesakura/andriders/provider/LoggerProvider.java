package com.eaglesakura.andriders.provider;

import com.eaglesakura.android.garnet.Depend;
import com.eaglesakura.android.garnet.Provide;
import com.eaglesakura.android.garnet.Provider;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.LogUtil;

import android.content.Context;
import android.util.Log;

public class LoggerProvider implements Provider {

    public static final String NAME_DEFAULT = "fw.default";

    public static final String NAME_APPLOG = "app.default";

    Context mContext;

    boolean mDebugable;

    @Depend(require = true)
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void onDependsCompleted(Object inject) {
        mDebugable = ContextUtil.isDebug(mContext);
    }

    @Override
    public void onInjectCompleted(Object inject) {
        LogUtil.setLogEnable("App.Broadcast", false);
    }

    /**
     * デフォルトで使用するロガーを指定する
     */
    @Provide(name = NAME_DEFAULT)
    public LogUtil.Logger provideDefaultLogger() {
        return new LogUtil.AndroidLogger(Log.class).setStackInfo(mDebugable);
    }

    @Provide(name = NAME_APPLOG)
    public LogUtil.Logger provideAppLogger() {
        return new LogUtil.AndroidLogger(Log.class) {
            @Override
            protected int getStackDepth() {
                return super.getStackDepth() + 1;
            }
        }.setStackInfo(mDebugable);
    }
}
