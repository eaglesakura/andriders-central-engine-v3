package com.eaglesakura.andriders.basicui.command;

import com.eaglesakura.andriders.ui.base.AppBaseActivity;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class AppBootSettingActivity extends AppBaseActivity {
    @Override
    public int getDefaultLayoutId(@NonNull ContentHolderActivityDelegate self) {
        return 0;
    }

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return null;
    }
}
