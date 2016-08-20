package com.eaglesakura.andriders.ui.navigation.command;

import com.eaglesakura.andriders.ui.navigation.NavigationActivityTest;
import com.eaglesakura.andriders.ui.navigation.NavigationBaseFragment;

import org.junit.Test;

public class CommandSettingFragmentMainTest extends NavigationActivityTest {

    @Test
    public void 正常にFragmentが起動できる() throws Exception {
        NavigationBaseFragment fragment = getNavigationFragment(TargetStub.class);
        assertNotNull(fragment);
    }

    public static class TargetStub extends CommandSettingFragmentMain {

    }
}