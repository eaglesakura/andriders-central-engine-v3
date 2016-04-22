package com.eaglesakura.andriders.ui.base;

import com.eaglesakura.android.framework.delegate.fragment.DialogFragmentDelegate;

import android.app.Dialog;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * アプリ内でのダイアログ管理を行う
 */
public abstract class AppDialogFragment extends AppBaseFragment implements DialogFragmentDelegate.SupportDialogFragmentCompat {
    private final DialogFragmentDelegate mDialogFragmentDelegate = new DialogFragmentDelegate(this, mLifecycleDelegate);

    @Nullable
    public Dialog getDialog() {
        return mDialogFragmentDelegate.getDialog();
    }

    /**
     * ダイアログ表示を行う
     */
    public void show(Fragment parent) {
        mDialogFragmentDelegate.onShow(getChildFragmentManager(), getClass().getName());
    }

    @Override
    public Fragment getFragment(DialogFragmentDelegate self) {
        return this;
    }
}
