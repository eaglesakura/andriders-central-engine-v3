package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.info.InformationFragmentMain;
import com.eaglesakura.andriders.ui.widget.ImageLoaderFragment;
import com.eaglesakura.sloth.app.delegate.ContentHolderActivityDelegate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * アプリ情報を表示するActivity
 */
public class InformationActivity extends AppNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            ImageLoaderFragment.attach(this);
        }
    }

    @Override
    public int getContentLayout(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.system_activity_with_toolbar;
    }

    @NonNull
    @Override
    public Fragment newContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new InformationFragmentMain();
    }

}
