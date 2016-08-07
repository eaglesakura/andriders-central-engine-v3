package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.ui.navigation.NavigationActivityTest;
import com.eaglesakura.andriders.ui.navigation.NavigationBaseFragment;

import org.junit.Test;

public class DisplaySettingFragmentMainTest extends NavigationActivityTest {

    @Test
    public void Fragmentが起動できる() throws Throwable {
        NavigationBaseFragment fragment = getNavigationFragment(DisplaySettingFragmentMain.class);
        assertNotNull(fragment);

        sleep(1000);
    }
}