package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.AppScenarioTest;
import com.eaglesakura.andriders.R;
import com.eaglesakura.android.devicetest.scenario.UiScenario;

import org.junit.Test;

import static com.eaglesakura.android.devicetest.scenario.ScenarioContext.assertTopActivity;

public class ProfileSettingActivityTest extends AppScenarioTest<ProfileSettingActivity> {

    public ProfileSettingActivityTest() {
        super(ProfileSettingActivity.class);
    }

    @Test
    public void ホイール選択が行える() throws Throwable {
        assertTopActivity(ProfileSettingActivity.class);
        for (int i = 0; i < 10; ++i) {
            // 適当に選択させる
            UiScenario.fromId(R.id.Selector_WheelOuterLength).click().step();
            UiScenario.fromId(R.id.Content_Holder_Root).monkeyClick(1).step();
        }
    }
}