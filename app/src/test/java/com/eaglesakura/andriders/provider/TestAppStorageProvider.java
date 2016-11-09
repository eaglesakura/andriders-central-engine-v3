package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.storage.AppStorageManager;
import com.eaglesakura.util.IOUtil;

import java.io.File;

public class TestAppStorageProvider extends AppStorageProvider {
    @Override
    public AppStorageManager provideStorageController() {
        return new AppStorageManager(getApplication()) {
            @Override
            protected File getExternalDataStorage() {
                return IOUtil.mkdirs(new File(getApplication().getFilesDir(), "sdcard"));
            }
        };
    }
}
