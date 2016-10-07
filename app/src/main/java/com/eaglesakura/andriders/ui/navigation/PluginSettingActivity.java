package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.plugin.PluginSettingFragmentMain;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * プラグイン設定画面を構築する
 */
public class PluginSettingActivity extends AppNavigationActivity {

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new PluginSettingFragmentMain();
    }
}
