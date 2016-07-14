package com.eaglesakura.andriders.basicui.command;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.util.Util;

import org.junit.Rule;
import org.junit.Test;

import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;

import java.util.List;

import static org.junit.Assert.*;

public class AppLauncherSelectActivityTest extends AppDeviceTestCase {

    @Rule
    public final ActivityTestRule<AppLauncherSelectActivity> mRule = new ActivityTestRule<>(AppLauncherSelectActivity.class);

    @NonNull
    AppLauncherSelectActivity mActivity;

    @Override
    public void onSetup() {
        super.onSetup();
        mActivity = mRule.getActivity();
        assertNotNull(mActivity);
    }

    @Test
    public void 起動が正常に行える() throws Throwable {
        Util.sleep(100);
    }

    @Test
    public void アプリの列挙が正常に行える() throws Throwable {
        List<ResolveInfo> infos = mActivity.listLauncherApplications();
        assertNotEquals(infos.size(), 0);
        for (ResolveInfo info : infos) {
            AppLog.test("package[%s] activity[%s]", info.activityInfo.packageName, info.activityInfo.name);
        }
    }
}