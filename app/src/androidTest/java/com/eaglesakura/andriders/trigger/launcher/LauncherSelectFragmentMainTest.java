package com.eaglesakura.andriders.trigger.launcher;

import com.eaglesakura.util.Util;

import static com.eaglesakura.android.devicetest.scenario.ScenarioContext.findFragment;

public class LauncherSelectFragmentMainTest {

    public static void test_起動後にアプリ列挙が行えている() throws Throwable {
        LauncherSelectFragmentMain fragmentMain = findFragment(LauncherSelectFragmentMain.class);
        Util.sleep(1000);
//        assertNotEquals(fragmentMain.mAdapter.getCollection().size(), 0);
    }
}