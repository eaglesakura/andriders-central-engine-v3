package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.log.TotalLogFragmentMain;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;
import com.eaglesakura.material.widget.support.SupportProgressFragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * 全体ログサマリ表示用Activity
 */
public class TotalLogActivity extends AppNavigationActivity implements TotalLogFragmentMain.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            SupportProgressFragment.attach(this, R.id.Root);
        }
    }

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new TotalLogFragmentMain();
    }

    @Override
    public void onSessionNotFound(TotalLogFragmentMain self) {

    }

    @Override
    public void onSessionLoadFailed(TotalLogFragmentMain self, Throwable error) {
        AppDialogBuilder.newError(this, error)
                .positiveButton(R.string.Word_Common_OK, () -> finish())
                .cancelable(false)
                .show(mLifecycleDelegate);
    }
}
