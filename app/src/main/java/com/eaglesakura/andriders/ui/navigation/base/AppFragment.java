package com.eaglesakura.andriders.ui.navigation.base;

import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.SupportFragment;
import com.eaglesakura.material.widget.support.SupportProgressFragment;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.Menu;

/**
 * アプリの制御Fragment
 */
public class AppFragment extends SupportFragment {
    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {

    }

    @Override
    public void onAfterBindMenu(SupportFragmentDelegate self, Menu menu) {

    }

    @Override
    public void onAfterInjection(SupportFragmentDelegate self) {

    }

    @NonNull
    public ProgressToken pushProgress(@StringRes int stringRes) {
        return pushProgress(getString(stringRes));
    }

    @NonNull
    public ProgressToken pushProgress(String message) {
        return SupportProgressFragment.pushProgress(this, message);
    }
}
