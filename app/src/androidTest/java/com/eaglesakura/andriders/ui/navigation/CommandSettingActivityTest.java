package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.AppScenarioTest;
import com.eaglesakura.andriders.R;
import com.eaglesakura.android.devicetest.scenario.UiScenario;

import org.junit.Test;

public class CommandSettingActivityTest extends AppScenarioTest<CommandSettingActivity> {

    public CommandSettingActivityTest() {
        super(CommandSettingActivity.class);
    }

    @Test
    public void Activityが起動できる() throws Throwable {
        sleep(1000);

        UiScenario.fromId(R.id.Content_Holder_Root)
                .swipeRightToLeft().sleep(500)
                .swipeRightToLeft().sleep(500)
                .swipeRightToLeft().sleep(500)
                .swipeRightToLeft().sleep(500)
        ;
    }

    @Test
    public void タブをランダムで切り替える() throws Throwable {
        sleep(1000);

        for (int i = 0; i < 5; ++i) {
            UiScenario.fromId(R.id.Content_PagerTab).monkeyClick(10, 100, 300);
        }
    }
}