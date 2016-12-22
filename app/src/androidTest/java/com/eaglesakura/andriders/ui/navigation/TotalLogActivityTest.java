package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.AppScenarioTest;
import com.eaglesakura.andriders.R;
import com.eaglesakura.android.devicetest.scenario.UiScenario;

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

    @Test
    public void 適当なアイテムを開ける() throws Throwable {
        assertTopActivity(TotalLogActivity.class);

        UiScenario.fromId(R.id.Content_List).click(0.5, 0.75).longStep();
    }
}