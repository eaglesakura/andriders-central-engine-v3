package com.eaglesakura.andriders.ui.navigation.base;

import com.eaglesakura.andriders.ui.navigation.common.AppProgressFragment;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.progress.ProgressStackManager;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.SupportFragment;

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
        AppProgressFragment fragment = findInterfaceOrThrow(AppProgressFragment.class);
        ProgressStackManager progressStackManager = fragment.getProgressStackManager();
        ProgressToken token = ProgressToken.fromMessage(progressStackManager, message);
        progressStackManager.push(token);
        return token;
    }
}
