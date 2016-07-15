package com.eaglesakura.andriders.basicui.command;

import com.eaglesakura.andriders.AceApplication;
import com.eaglesakura.andriders.AppDeviceTestUtil;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.devicetest.DeviceActivityTestCase;
import com.eaglesakura.util.Util;

import org.junit.Test;

import android.content.pm.ResolveInfo;

import java.util.List;

import static org.junit.Assert.*;

public class LauncherSelectActivityTest extends DeviceActivityTestCase<LauncherSelectActivity, AceApplication> {

    public LauncherSelectActivityTest() {
        super(LauncherSelectActivity.class);
    }

    @Override
    public void onSetup() {
        super.onSetup();
        AppDeviceTestUtil.onSetup(this);
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        AppDeviceTestUtil.onShutdown(this);
    }

    @Test
    public void 起動が正常に行える() throws Throwable {
        getActivity();
        Util.sleep(100);
    }

    @Test
    public void アプリの列挙が正常に行える() throws Throwable {
        List<ResolveInfo> infoList = getActivity().listLauncherApplications();
        assertNotEquals(infoList.size(), 0);
        for (ResolveInfo info : infoList) {
            AppLog.test("package[%s] activity[%s]", info.activityInfo.packageName, info.activityInfo.name);
        }
    }
}