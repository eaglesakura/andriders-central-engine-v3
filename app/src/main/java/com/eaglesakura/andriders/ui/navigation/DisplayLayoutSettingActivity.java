package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.display.DisplaySettingFragmentMain;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * サイコンディスプレイ表示内容セットアップ
 */
public class DisplayLayoutSettingActivity extends AppNavigationActivity {

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new DisplaySettingFragmentMain();
    }
}
