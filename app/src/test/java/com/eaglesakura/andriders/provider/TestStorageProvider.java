package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.db.AppSettings;
import com.eaglesakura.andriders.db.AppStorageManager;
import com.eaglesakura.util.RandomUtil;

import java.io.File;

import static org.junit.Assert.*;

public class TestStorageProvider extends StorageProvider {

    static String sDatabasePath;

    static int sCallStorageManagerCount;

    static int sCallSettingsCount;

    public static void onSetup(AppUnitTestCase testCase) {
        sDatabasePath = "test." + RandomUtil.randString(5) + testCase.hashCode();
    }

    @Override
    public AppStorageManager provideStorageManager() {
        assertEquals(++sCallStorageManagerCount, 1);    // シングルトンであることを保証する

        AppStorageManager storageManager = new AppStorageManager(mContext) {
            @Override
            protected File getDatabaseDirectory() {
                return new File(super.getDatabaseDirectory(), sDatabasePath);
            }
        };
        storageManager.makeDirectory();
        return storageManager;
    }

    @Override
    public AppSettings provideSettings() {
        assertEquals(++sCallSettingsCount, 1);  // シングルトンであることを保証する

        AppSettings settings = super.provideSettings();

        // 計算を確定させるため、フィットネスデータを構築する
        // 計算しやすくするため、データはキリの良い数にしておく
        settings.getUserProfiles().setUserWeight(AppUnitTestCase.USER_WEIGHT);
        settings.getUserProfiles().setNormalHeartrate(90);
        settings.getUserProfiles().setMaxHeartrate(190);
        settings.getUserProfiles().setWheelOuterLength(2096); // 700 x 23c

        return settings;
    }
}
