package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.command.CommandSettingFragmentMain;
import com.eaglesakura.andriders.ui.navigation.menu.MenuController;
import com.eaglesakura.android.framework.BuildConfig;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

public class NavigationActivity extends NavigationBaseActivity {

    @Bind(R.id.Content_Drawer)
    DrawerLayout mDrawerLayout;

    MenuController mMenuController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMenuController = new MenuController(this,
                findViewById(NavigationView.class, R.id.Content_Navigation_Root),
                mDrawerLayout
        );
        mMenuController.setCallback(mMenuCallback);

        if (!BuildConfig.DEBUG) {
            UIHandler.postDelayedUI(() -> {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }, 500);
        }
    }

    @Override
    public int getDefaultLayoutId(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.activity_content_with_drawer;
    }

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
//        return new AppBootFragmentMain();
        return new CommandSettingFragmentMain();
//        return UserLogMain.newInstance(this);
//        return DisplaySettingFragmentMain.newInstance(this);
//        return GpxImportTourFragmentMain.newInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMenuController.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMenuController.onPause();
    }

    final MenuController.MenuCallback mMenuCallback = (fragment) -> {
        nextNavigation(fragment, 0x00);
    };
}
