package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.storage.AppStorageController;
import com.eaglesakura.android.framework.provider.ContextProvider;
import com.eaglesakura.android.garnet.Provide;
import com.eaglesakura.android.garnet.Singleton;

/**
 * アプリ内での各種Controllerを依存解決するProvider
 */
@Singleton
public class AppControllerProvider extends ContextProvider {
    @Provide
    public AppStorageController provideStorageController() {
        return new AppStorageController(getApplication());
    }
}
