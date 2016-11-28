package com.eaglesakura.andriders.trigger;

import com.eaglesakura.andriders.AppScenarioTest;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.trigger.launcher.LauncherSelectFragmentMain;
import com.eaglesakura.andriders.trigger.launcher.LauncherSelectFragmentMainTest;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.devicetest.scenario.ScenarioContext;
import com.eaglesakura.android.devicetest.scenario.UiScenario;

import org.junit.Test;

import android.app.Activity;
import android.content.pm.ResolveInfo;

import java.util.List;

public class LauncherSelectActivityTest extends AppScenarioTest<LauncherSelectActivity> {
    public LauncherSelectActivityTest() {
        super(LauncherSelectActivity.class);
    }

    @Test
    public void 起動が正常に行える() throws Throwable {
        sleep(1000);
    }

    @Test
    public void 起動後にアプリ列挙が行えている() throws Throwable {
        sleep(1000);
        LauncherSelectFragmentMainTest.test_起動後にアプリ列挙が行えている();
    }

    @Test
    public void アイテムがタップされたらfinishされる() throws Throwable {
        Activity activity = ScenarioContext.getTopActivity();
        sleep(1000);
        UiScenario.fromId(R.id.Content_Holder_Root)
                .swipeBottomToTop().step()
                .swipeTopToBottom().step()
                .click(0.5, 0.4).step();
        sleep(500);
        assertTrue(activity.isFinishing());
    }

    @Test
    public void アプリの列挙が正常に行える() throws Throwable {
        List<ResolveInfo> infoList = LauncherSelectFragmentMain.listLauncherApplications(getContext());
        validate(infoList).notEmpty().allNotNull().each(info -> {
            AppLog.test("package[%s] activity[%s]", info.activityInfo.packageName, info.activityInfo.name);
        });
    }
}