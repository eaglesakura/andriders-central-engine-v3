package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.profile.UserProfileSettingFragmentMain;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * ユーザー設定を行う
 */
public class ProfileSettingActivity extends AppNavigationActivity {

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new UserProfileSettingFragmentMain();
    }
}
