package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.AppDeviceTestUtil;
import com.eaglesakura.andriders.storage.AppStorageManager;
import com.eaglesakura.android.garnet.Provide;
import com.eaglesakura.android.garnet.Singleton;

import java.io.File;

@Singleton
public class DeviceTestAppStorageProvider extends AppStorageProvider {

    @Provide
    @Override
    public AppStorageManager provideStorageController() {
        return new AppStorageManager(getApplication()) {
            @Override
            protected File getDataStoragePath() {
                return AppDeviceTestUtil.getExternalStoragePath();
            }
        };
    }
}
