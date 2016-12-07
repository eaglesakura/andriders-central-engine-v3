package com.eaglesakura.andriders.ui.navigation.session;

import com.eaglesakura.andriders.AppScenarioTest;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.plugin.connection.SessionControlConnection;
import com.eaglesakura.andriders.ui.navigation.UserSessionActivity;
import com.eaglesakura.android.devicetest.scenario.UiScenario;
import com.eaglesakura.util.Util;

import org.junit.Test;

import static com.eaglesakura.android.devicetest.scenario.ScenarioContext.assertTopActivity;

public class UserSessionActivityTest extends AppScenarioTest<UserSessionActivity> {
    public UserSessionActivityTest() {
        super(UserSessionActivity.class);
    }

    @Test
    public void Activityが開ける() throws Throwable {
        assertTopActivity(UserSessionActivity.class);
    }

    @Test
    public void セッションを開始できる() throws Throwable {
        SessionControlConnection connection = new SessionControlConnection(getContext());
        connection.connect(() -> false);
        Util.sleep(500);

        // UI経由でセッションを介し
        UiScenario.clickFromId(R.id.Button_SessionChange).step();
        UiScenario.clickFromText(R.string.Word_Central_SessionStart).step();

        // しばらくしたらセッションが開始されている
        Util.sleep(1000);
        assertTrue(connection.getCentralSessionController().isSessionStarted());

        connection.disconnect(() -> false);
    }
}