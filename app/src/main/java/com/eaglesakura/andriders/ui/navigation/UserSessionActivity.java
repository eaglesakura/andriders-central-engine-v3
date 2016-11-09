package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.session.MenuController;
import com.eaglesakura.andriders.ui.navigation.session.UserSessionFragmentMain;
import com.eaglesakura.andriders.ui.widget.ImageLoaderFragment;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.material.widget.ToolbarBuilder;
import com.eaglesakura.material.widget.support.SupportProgressFragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;

/**
 * アプリ起動後のデフォルトActivity
 */
public class UserSessionActivity extends AppNavigationActivity {

    @Bind(R.id.Content_Drawer)
    DrawerLayout mDrawerLayout;

    ActionBarDrawerToggle mDrawerToggle;

    MenuController mMenuController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            ImageLoaderFragment.attach(this);
            SupportProgressFragment.attach(this, R.id.Root);

            // Activityの初回起動時のみ、タイミングをずらして開く
            UIHandler.postDelayedUI(() -> {
                mDrawerLayout.openDrawer(GravityCompat.START);
                mDrawerToggle.syncState();
            }, 250);
        }

        MargarineKnife.bind(this);

        // Toolbarを構築
        ToolbarBuilder toolbarBuilder = ToolbarBuilder.from(this)
                .drawer(mDrawerLayout, R.string.Env_AppName, R.string.Env_AppName)
                .build();

        mDrawerToggle = toolbarBuilder.getDrawerToggle();

        // メニューを構築する
        mMenuController = new MenuController(toolbarBuilder, mMenuCallbackImpl).bind(mLifecycleDelegate);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getDefaultLayoutId(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.session_info_activity;
    }

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new UserSessionFragmentMain();
    }

    UserSessionActivity self() {
        return this;
    }

    /**
     * メニューハンドリング
     */
    final MenuController.Callback mMenuCallbackImpl = new MenuController.Callback() {
        @Override
        public void requestShowPlugins(MenuController self) {
            Intent intent = new Intent(self(), PluginSettingActivity.class);
            startActivity(intent);
        }

        @Override
        public void requestShowInformation(MenuController self) {
            Intent intent = new Intent(self(), InformationActivity.class);
            startActivity(intent);
        }

        @Override
        public void requestShowCommands(MenuController self) {
            Intent intent = new Intent(self(), CommandSettingActivity.class);
            startActivity(intent);
        }
    };
}
