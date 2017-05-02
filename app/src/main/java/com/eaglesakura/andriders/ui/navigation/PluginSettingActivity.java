package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.plugin.PluginSettingFragmentMain;
import com.eaglesakura.sloth.app.delegate.ContentHolderActivityDelegate;
import com.eaglesakura.sloth.ui.progress.SupportProgressFragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * プラグイン設定画面を構築する
 */
public class PluginSettingActivity extends AppNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            SupportProgressFragment.attach(this, R.id.Root);
        }
    }

    @Override
    public int getContentLayout(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.system_activity_with_toolbar;
    }

    @NonNull
    @Override
    public Fragment newContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new PluginSettingFragmentMain();
    }

}
