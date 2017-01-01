package com.eaglesakura.andriders.ui.navigation.base;

import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.progress.DialogToken;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.SupportFragment;
import com.eaglesakura.material.widget.DialogBuilder;
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

    @NonNull
    public DialogToken showProgress(@StringRes int messageRes) {
        return showProgress(getString(messageRes));
    }

    @NonNull
    public DialogToken showProgress(String message) {
        DialogBuilder builder = AppDialogBuilder.newProgress(getContext(), message);
        builder.cancelable(false);
        return DialogBuilder.showAsToken(builder, mLifecycleDelegate);
    }
}
