package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.system.context.AppSettings;

import static junit.framework.Assert.assertEquals;

public class TestAppContextProvider extends AppContextProvider {
    static int sCallSettingsCount;


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
