package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.boot.AppBootFragmentMain;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class AppBootActivity extends NavigationBaseActivity {
    @Override
    public int getDefaultLayoutId(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.activity_content;
    }

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new AppBootFragmentMain();
    }

}
