package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.android.garnet.Provide;
import com.eaglesakura.android.garnet.Singleton;
import com.eaglesakura.sloth.provider.ContextProvider;

import android.content.Context;

/**
 * アプリを動作させるためのContextを依存させるProvider
 */
@Singleton
public class AppContextProvider extends ContextProvider {


    static AppSettings sAppSettings;

    static AppSettings getAppSettings(Context context) {
        if (sAppSettings == null) {
            sAppSettings = new AppSettings(context.getApplicationContext());
        }
        return sAppSettings;
    }

    @Provide
    public AppSettings provideSettings() {
        return getAppSettings(getContext());
    }
}
