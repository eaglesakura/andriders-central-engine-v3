package com.eaglesakura.andriders.ui.navigation.command;

import com.eaglesakura.andriders.ui.navigation.NavigationActivityTest;

import org.junit.Test;

import static com.eaglesakura.android.devicetest.scenario.ScenarioContext.findFragment;

public class CommandSettingFragmentMainTest extends NavigationActivityTest {

    @Test
    public void 正常にFragmentが起動できる() throws Exception {
        validate(findFragment(CommandSettingFragmentMain.class))
                .notNull();
    }

    public static class TargetStub extends CommandSettingFragmentMain {

    }
}