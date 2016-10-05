package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.AppUnitTestCase;

public class TestAppManagerProvider extends AppControllerProvider {

//    static String sDatabasePath;
//
//    static int sCallStorageManagerCount;
//
    public static void onSetup(AppUnitTestCase testCase) {
//        sDatabasePath = "test." + RandomUtil.randString(5) + testCase.hashCode();
    }
//
//    @Override
//    public AppStorageController provideStorageManager() {
//        assertEquals(++sCallStorageManagerCount, 1);    // シングルトンであることを保証する
//
//        AppStorageController storageManager = new AppStorageController(getContext()) {
//            @Override
//            protected File getDatabaseDirectory() {
//                return new File(super.getDatabaseDirectory(), sDatabasePath);
//            }
//        };
//        return storageManager;
//    }
}
