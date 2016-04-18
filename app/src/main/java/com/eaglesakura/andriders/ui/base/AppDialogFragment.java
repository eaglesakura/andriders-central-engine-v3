package com.eaglesakura.andriders.ui.base;

import com.eaglesakura.android.framework.ui.delegate.DialogFragmentDelegate;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * アプリ内でのダイアログ管理を行う
 */
public abstract class AppDialogFragment extends AppBaseFragment {
    private DialogFragmentDelegate mDelegate = new DialogFragmentDelegate(new DialogFragmentDelegate.SupportDialogFragmentCompat() {
        @NonNull
        @Override
        public Dialog onCreateDialog(DialogFragmentDelegate self, Bundle savedInstanceState) {
            return AppDialogFragment.this.onCreateDialog(savedInstanceState);
        }

        @Override
        public void onDismiss(DialogFragmentDelegate self) {

        }

        @Override
        public Fragment getFragment(DialogFragmentDelegate self) {
            return AppDialogFragment.this;
        }
    });


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate.onCreate(savedInstanceState);
    }

    @Nullable
    public Dialog getDialog() {
        return mDelegate.getDialog();
    }

    /**
     * ダイアログの生成を行わせる
     *
     * この呼出が行われたタイミングでは、全ての@Stateオブジェクトはレストア済である。
     */
    @NonNull
    protected abstract Dialog onCreateDialog(Bundle savedInstanceState);

    /**
     * ダイアログ表示を行う
     */
    public void show(Fragment parent) {
        mDelegate.onShow(parent.getChildFragmentManager(), getClass().getName());
    }
}
