package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.db.AppStorageController;
import com.eaglesakura.util.RandomUtil;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class TestAppManagerProvider extends AppControllerProvider {

    static String sDatabasePath;

    static int sCallStorageManagerCount;

    public static void onSetup(AppUnitTestCase testCase) {
        sDatabasePath = "test." + RandomUtil.randString(5) + testCase.hashCode();
    }

    @Override
    public AppStorageController provideStorageManager() {
        assertEquals(++sCallStorageManagerCount, 1);    // シングルトンであることを保証する

        AppStorageController storageManager = new AppStorageController(getContext()) {
            @Override
            protected File getDatabaseDirectory() {
                return new File(super.getDatabaseDirectory(), sDatabasePath);
            }
        };
        storageManager.makeDirectory();
        return storageManager;
    }
}
