package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.data.AppSettings;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;

import android.content.Context;

/**
 * ローカル変数に対してinjectを行いたい場合のUtil
 */
public class ProviderUtil {

    static AppSettings sAppSettings;

    public synchronized static AppSettings getAppSettings(Context context) {
        if (sAppSettings == null) {
            sAppSettings = new AppSettings(context.getApplicationContext());
        }
        return sAppSettings;
    }

    static class ContextRef {
        @Inject(AppContextProvider.class)
        Context mContext;
    }

    static class AppSettingsRef {
        @Inject(AppContextProvider.class)
        AppSettings mAppSettings;
    }

    public static Context provideContext() {
        return Garnet.inject(new ContextRef()).mContext;
    }

    public static AppSettings provideAppSettings() {
        return Garnet.inject(new AppSettingsRef()).mAppSettings;
    }
}
