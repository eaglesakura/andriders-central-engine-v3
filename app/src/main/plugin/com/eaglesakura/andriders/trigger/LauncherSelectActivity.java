package com.eaglesakura.andriders.trigger;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.trigger.launcher.LauncherSelectFragmentMain;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.sloth.app.delegate.ContentHolderActivityDelegate;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class LauncherSelectActivity extends AppNavigationActivity {

    @Override
    public int getContentLayout(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.system_activity_with_toolbar;
    }

    @NonNull
    @Override
    public Fragment newContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new LauncherSelectFragmentMain();
    }
}
