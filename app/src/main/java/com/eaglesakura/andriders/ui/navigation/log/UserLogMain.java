package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;
import com.eaglesakura.andriders.ui.navigation.MainContentActivity;
import com.eaglesakura.andriders.ui.navigation.log.gpx.GpxTourFragmentMain;
import com.eaglesakura.android.margarine.OnMenuClick;

import android.content.Context;
import android.os.Bundle;

/**
 * ログ表示画面のメインFragment
 */
public class UserLogMain extends BaseNavigationFragment {
    public UserLogMain() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_userlog_main);
        mFragmentDelegate.setOptionMenuId(R.menu.fragment_userlog_import);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
        }
    }

    @OnMenuClick(R.id.UserLog_Import_GPX)
    public void clickImportGpx() {
        nextNavigation(new GpxTourFragmentMain(), MainContentActivity.NAVIGATION_FLAG_BACKSTACK);
    }

    public static UserLogMain newInstance(Context context) {
        return new UserLogMain();
    }
}
