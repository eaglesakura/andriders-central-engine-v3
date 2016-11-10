package com.eaglesakura.andriders.trigger;

import com.eaglesakura.andriders.trigger.launcher.LauncherSelectFragmentMain;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class LauncherSelectActivity extends AppNavigationActivity {

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new LauncherSelectFragmentMain();
//        return null;
    }

}
