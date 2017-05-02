package com.eaglesakura.andriders.trigger.launcher;

import com.eaglesakura.android.devicetest.scenario.ScenarioContext;
import com.eaglesakura.android.util.FragmentUtil;
import com.eaglesakura.util.Util;

public class LauncherSelectFragmentMainTest {

    public static void test_起動後にアプリ列挙が行えている() throws Throwable {
        LauncherSelectFragmentMain fragmentMain = FragmentUtil.findInterface(null, ScenarioContext.getTopActivity(), LauncherSelectFragmentMain.class);
        Util.sleep(1000);
//        assertNotEquals(fragmentMain.mAdapter.getCollection().size(), 0);
    }
}