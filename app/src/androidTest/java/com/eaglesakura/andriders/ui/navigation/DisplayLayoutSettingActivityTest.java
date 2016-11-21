package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.AppScenarioTest;
import com.eaglesakura.andriders.R;
import com.eaglesakura.android.devicetest.scenario.UiScenario;

import org.junit.Test;

public class DisplayLayoutSettingActivityTest extends AppScenarioTest<DisplayLayoutSettingActivity> {
    public DisplayLayoutSettingActivityTest() {
        super(DisplayLayoutSettingActivity.class);
    }

    @Test
    public void アプリ切り替えが行える() throws Throwable {
        UiScenario.clickFromId(R.id.Content_Holder_AppSelector).step();
        UiScenario.clickFromId(R.id.Content_Holder_Root).step();
    }

    @Test
    public void DisplayKey設定が行える() throws Throwable {
        UiScenario.clickFromId(R.id.Content_Holder_DisplayLayout).monkeyClick(1).longStep();
        UiScenario.clickFromId(R.id.Content_Holder_Root).monkeyClick(1).longStep();
    }
}