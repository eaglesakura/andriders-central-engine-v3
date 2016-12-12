package com.eaglesakura.andriders.ui.navigation.boot;

import com.eaglesakura.andriders.AppScenarioTest;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.AppBootActivity;
import com.eaglesakura.andriders.ui.navigation.UserSessionActivity;
import com.eaglesakura.util.Util;

import org.junit.Test;

import static com.eaglesakura.android.devicetest.scenario.ScenarioContext.assertTopActivity;
import static com.eaglesakura.android.devicetest.scenario.UiScenario.fromText;

public class AppBootActivityTest extends AppScenarioTest<AppBootActivity> {

    public AppBootActivityTest() {
        super(AppBootActivity.class);
    }

    @Test(timeout = 1000 * 60)
    public void 起動処理を完了できる() throws Throwable {
        AppBootActivity activity = mRule.getActivity();

        while (!activity.isFinishing()) {
            Util.sleep(1);
        }

        // セッション管理画面が開いている
        assertTopActivity(UserSessionActivity.class);
        Util.sleep(500);

        // エラーダイアログが表示されていない
        assertNull(fromText(R.string.Word_Common_OK).get());
    }
}