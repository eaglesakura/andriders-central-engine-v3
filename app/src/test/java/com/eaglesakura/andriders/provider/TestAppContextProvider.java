package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.system.context.AppSettings;

import static junit.framework.Assert.assertEquals;

public class TestAppContextProvider extends AppContextProvider {
    @Override
    public AppSettings provideSettings() {
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
