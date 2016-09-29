package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.AceApplication;
import com.eaglesakura.android.devicetest.DeviceActivityTestCase;

import android.support.v7.app.AppCompatActivity;

public abstract class NavigationActivityTest extends DeviceActivityTestCase<AppCompatActivity, AceApplication> {
    public NavigationActivityTest() {
        super(null);
    }
//
//    protected NavigationActivity getNavigationActivity(Class<? extends NavigationBaseFragment> clazz) {
//        Intent intent = new Intent();
//        intent.putExtra("EXTRA_CONTENT_FRAGMENT_CLASS", clazz.getName());
//        getActivity(intent);
//        Util.sleep(500);
//        return getActivity();
//    }
//
//    protected <T extends NavigationBaseFragment> T getNavigationFragment(Class<T> clazz) {
//        Intent intent = new Intent();
//        intent.putExtra("EXTRA_CONTENT_FRAGMENT_CLASS", clazz.getName());
//        NavigationActivity activity = getActivity(intent);
//        Util.sleep(500);
//        return (T) activity.getContentFragment();
//    }
}