package com.eaglesakura.andriders.basicui.command;

import com.eaglesakura.andriders.AceApplication;
import com.eaglesakura.andriders.AppDeviceTestUtil;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.command.CommandSetting;
import com.eaglesakura.andriders.db.command.CommandSetupData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.devicetest.DeviceActivityTestCase;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.material.widget.adapter.AdapterCollection;
import com.eaglesakura.util.Util;

import org.junit.Test;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v7.widget.RecyclerView;

import java.util.List;

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
    public void 起動後にアプリ列挙が行えている() throws Throwable {
        LauncherSelectActivity activity = getActivity();
        Util.sleep(1000);
        assertNotEquals(activity.mAdapter.getCollection().size(), 0);
    }

    @Test
    public void 生成されたIntentが正常な戻り値として構築されている() throws Throwable {
        Intent bootIntent = new Intent().putExtra(CommandSetting.EXTRA_COMMAND_KEY, CommandKey.fromProximity(1));
        LauncherSelectActivity activity = getActivity(bootIntent);
        Util.sleep(1000);

        UIHandler.await(() -> {
            AdapterCollection<ResolveInfo> collection = activity.mAdapter.getCollection();
            Intent intent = activity.onItemSelected(collection.get(0));
            assertTrue(activity.isFinishing());
            assertNotNull(intent);

            CommandSetupData result = CommandSetupData.getFromResult(intent);
            assertNotNull(result);

            return 0;
        });
    }

    @Test
    public void アイテムがタップされたらfinishされる() throws Throwable {
        LauncherSelectActivity activity = getActivity();
        Util.sleep(1000);
        Espresso.onView(ViewMatchers.isAssignableFrom(RecyclerView.class))
                .perform(RecyclerViewActions.scrollToPosition(20))
                .perform(RecyclerViewActions.actionOnItemAtPosition(20, ViewActions.click()));
        Util.sleep(500);
        assertTrue(activity.isFinishing());
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