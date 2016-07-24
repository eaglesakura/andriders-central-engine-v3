package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.db.AppSettings;
import com.eaglesakura.android.framework.provider.ContextProvider;
import com.eaglesakura.android.garnet.Provide;
import com.eaglesakura.android.garnet.Singleton;

/**
 * アプリを動作させるためのContextを依存させるProvider
 */
@Singleton
public class AppContextProvider extends ContextProvider {

    @Provide
    public AppSettings provideSettings() {
        return ProviderUtil.getAppSettings(getContext());
    }
}
