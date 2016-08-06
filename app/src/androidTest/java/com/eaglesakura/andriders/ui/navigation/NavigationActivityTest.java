package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.AceApplication;
import com.eaglesakura.android.devicetest.DeviceActivityTestCase;
import com.eaglesakura.util.Util;

import android.content.Intent;

public class NavigationActivityTest extends DeviceActivityTestCase<NavigationActivity, AceApplication> {
    public NavigationActivityTest() {
        super(NavigationActivity.class);
    }

    protected NavigationActivity getNavigationActivity(Class<? extends NavigationBaseFragment> clazz) {
        Intent intent = new Intent();
        intent.putExtra("EXTRA_CONTENT_FRAGMENT_CLASS", clazz.getName());
        return getActivity(intent);
    }

    protected <T extends NavigationBaseFragment> T getNavigationFragment(Class<? extends NavigationBaseFragment> clazz) {
        Intent intent = new Intent();
        intent.putExtra("EXTRA_CONTENT_FRAGMENT_CLASS", clazz.getName());
        NavigationActivity activity = getActivity(intent);
        Util.sleep(500);
        return (T) activity.getContentFragment();
    }
}