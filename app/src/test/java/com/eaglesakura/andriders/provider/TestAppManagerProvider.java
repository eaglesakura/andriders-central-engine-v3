package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.db.AppStorageManager;
import com.eaglesakura.util.RandomUtil;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class TestAppManagerProvider extends AppManagerProvider {

    static String sDatabasePath;

    static int sCallStorageManagerCount;

    public static void onSetup(AppUnitTestCase testCase) {
        sDatabasePath = "test." + RandomUtil.randString(5) + testCase.hashCode();
    }

    @Override
    public AppStorageManager provideStorageManager() {
        assertEquals(++sCallStorageManagerCount, 1);    // シングルトンであることを保証する

        AppStorageManager storageManager = new AppStorageManager(getContext()) {
            @Override
            protected File getDatabaseDirectory() {
                return new File(super.getDatabaseDirectory(), sDatabasePath);
            }
        };
        storageManager.makeDirectory();
        return storageManager;
    }
}
