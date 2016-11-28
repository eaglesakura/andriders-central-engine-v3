package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.info.InformationFragmentMain;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * アプリ情報を表示するActivity
 */
public class InformationActivity extends AppNavigationActivity {

    @Override
    public int getDefaultLayoutId(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.system_activity_with_toolbar;
    }

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new InformationFragmentMain();
    }
}
