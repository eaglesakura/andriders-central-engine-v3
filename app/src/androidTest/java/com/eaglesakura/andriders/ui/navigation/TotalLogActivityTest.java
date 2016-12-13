package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.AppScenarioTest;

import org.junit.Test;

import static com.eaglesakura.android.devicetest.scenario.ScenarioContext.assertTopActivity;

public class TotalLogActivityTest extends AppScenarioTest<TotalLogActivity> {
    public TotalLogActivityTest() {
        super(TotalLogActivity.class);
    }

    @Test
    public void Activityが開ける() throws Throwable {
        assertTopActivity(TotalLogActivity.class);
    }
}