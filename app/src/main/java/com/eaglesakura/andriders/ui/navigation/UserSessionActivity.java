package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.session.UserSessionFragmentMain;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * アプリ起動後のデフォルトActivity
 */
public class UserSessionActivity extends AppNavigationActivity {
    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new UserSessionFragmentMain();
    }
}
