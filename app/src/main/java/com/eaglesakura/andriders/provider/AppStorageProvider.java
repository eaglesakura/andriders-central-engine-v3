package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.storage.AppStorageManager;
import com.eaglesakura.android.framework.provider.ContextProvider;
import com.eaglesakura.android.garnet.Provide;
import com.eaglesakura.android.garnet.Singleton;

/**
 * アプリ内での各種Controllerを依存解決するProvider
 */
@Singleton
public class AppStorageProvider extends ContextProvider {
    @Provide
    public AppStorageManager provideStorageController() {
        return new AppStorageManager(getApplication());
    }
}
