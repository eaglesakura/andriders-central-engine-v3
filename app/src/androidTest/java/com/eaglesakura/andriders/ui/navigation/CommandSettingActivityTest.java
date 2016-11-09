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
                .swipeRightToLeft().sleep(1000);
    }
}