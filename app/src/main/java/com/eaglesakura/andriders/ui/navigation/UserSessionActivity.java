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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

/**
 * アプリ起動後のデフォルトActivity
 */
public class UserSessionActivity extends AppNavigationActivity {

    @Bind(R.id.Content_Drawer)
    DrawerLayout mDrawerLayout;

    MenuController mMenuController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            ImageLoaderFragment.attach(this);
            SupportProgressFragment.attach(this, R.id.Root);
        }

        MargarineKnife.bind(this);

        // Toolbarを構築
        ToolbarBuilder toolbarBuilder = ToolbarBuilder.from(this)
                .drawer(mDrawerLayout)
                .build();

        // メニューを構築する
        mMenuController = new MenuController(toolbarBuilder, mMenuCallbackImpl).bind(mLifecycleDelegate);

        // タイミングをずらしてDrawerを開く
        UIHandler.postDelayedUI(() -> mDrawerLayout.openDrawer(GravityCompat.START), 250);
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
    };
}
