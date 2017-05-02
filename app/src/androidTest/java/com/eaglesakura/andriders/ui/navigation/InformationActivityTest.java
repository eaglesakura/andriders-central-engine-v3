package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.AppScenarioTest;
import com.eaglesakura.andriders.R;
import com.eaglesakura.android.devicetest.scenario.UiScenario;
import com.eaglesakura.sloth.ui.license.LicenseViewActivity;

import org.junit.Test;

import static com.eaglesakura.android.devicetest.scenario.ScenarioContext.assertTopActivity;
import static com.eaglesakura.android.devicetest.scenario.ScenarioContext.getTopActivity;

public class InformationActivityTest extends AppScenarioTest<InformationActivity> {

    public InformationActivityTest() {
        super(InformationActivity.class);
    }

    @Test
    public void Activityが起動できる() throws Throwable {
        assertNotNull(getTopActivity(InformationActivity.class));
        sleep(1000);
    }

    @Test
    public void オープンソースリスト画面を開ける() throws Throwable {
        sleep(1000);

        UiScenario.fromId(R.id.Button_Licenses).click().step();
        assertTopActivity(LicenseViewActivity.class);
    }
}