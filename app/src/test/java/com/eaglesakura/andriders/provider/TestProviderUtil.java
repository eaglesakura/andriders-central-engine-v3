package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.storage.AppStorageManager;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;

public class TestProviderUtil {

    static class AppStorageManagerRef {
        @Inject(AppStorageProvider.class)
        AppStorageManager mAppStorageManager;
    }

    public static AppStorageManager provideAppStorageManager() {
        return Garnet.inject(new AppStorageManagerRef()).mAppStorageManager;
    }
}
