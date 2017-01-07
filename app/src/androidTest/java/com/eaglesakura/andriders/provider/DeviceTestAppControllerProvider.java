package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.AppDeviceTestUtil;
import com.eaglesakura.andriders.storage.AppStorageManager;

import java.io.File;

public class DeviceTestAppControllerProvider extends AppStorageProvider {
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
