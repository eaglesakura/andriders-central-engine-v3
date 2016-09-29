package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.system.AppStorageController;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;

public class TestProviderUtil {

    static class AppStorageManagerRef {
        @Inject(AppControllerProvider.class)
        AppStorageController mAppStorageManager;
    }

    public static AppStorageController provideAppStorageManager() {
        return Garnet.inject(new AppStorageManagerRef()).mAppStorageManager;
    }
}
