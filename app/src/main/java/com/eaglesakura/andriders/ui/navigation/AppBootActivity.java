package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.boot.AppBootFragmentMain;
import com.eaglesakura.sloth.app.delegate.ContentHolderActivityDelegate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * アプリの起動処理を行うActivity
 */
public class AppBootActivity extends AppNavigationActivity implements AppBootFragmentMain.Listener {

    @Override
    public int getContentLayout(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.activity_content_holder_notoolbar;
    }

    @NonNull
    @Override
    public Fragment newContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new AppBootFragmentMain();
    }

    @Override
    public void onBootCompleted(AppBootFragmentMain self) {
        Intent intent = new Intent(this, UserSessionActivity.class);
        startActivity(intent);
        finish();
    }
}
