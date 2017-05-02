package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.AppScenarioTest;
import com.eaglesakura.andriders.ui.navigation.plugin.PluginSettingFragmentMain;
import com.eaglesakura.android.devicetest.scenario.ScenarioContext;
import com.eaglesakura.android.util.FragmentUtil;
import com.eaglesakura.util.Util;

import org.junit.Test;

import static com.eaglesakura.android.devicetest.scenario.ScenarioContext.assertTopActivity;

public class PluginSettingActivityTest extends AppScenarioTest<PluginSettingActivity> {
    public PluginSettingActivityTest() {
        super(PluginSettingActivity.class);
    }

    @Test
    public void Activityが起動できる() throws Throwable {
        assertTopActivity(PluginSettingActivity.class);
    }

    @Test
    public void Pluginが読み込めている() throws Throwable {
        Util.sleep(1000);
        PluginSettingFragmentMain fragment = FragmentUtil.findInterface(null, ScenarioContext.getTopActivity(), PluginSettingFragmentMain.class);
        assertNotNull(fragment);

        validate(fragment.getPlugins().list()).allNotNull().sizeFrom(1);
    }
}