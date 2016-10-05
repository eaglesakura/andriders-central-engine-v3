package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.AppDeviceTestUtil;
import com.eaglesakura.andriders.storage.AppStorageController;

import java.io.File;

public class DeviceTestAppControllerProvider extends AppControllerProvider {
    @Override
    public AppStorageController provideStorageController() {
        return new AppStorageController(getApplication()) {
            @Override
            protected File getExternalDataStorage() {
                return AppDeviceTestUtil.getExternalStoragePath();
            }
        };
    }
}
