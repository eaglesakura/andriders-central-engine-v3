package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.AppScenarioTest;

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

    }
}