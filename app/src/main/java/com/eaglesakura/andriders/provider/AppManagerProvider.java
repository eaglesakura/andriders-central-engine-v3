package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.db.AppStorageManager;
import com.eaglesakura.android.framework.provider.ContextProvider;
import com.eaglesakura.android.garnet.Provide;
import com.eaglesakura.android.garnet.Singleton;

/**
 * アプリ内での各種Managerを依存解決するProvider
 */
@Singleton
public class AppManagerProvider extends ContextProvider {
    @Provide
    public AppStorageManager provideStorageManager() {
        return new AppStorageManager(getApplication());
    }
}
