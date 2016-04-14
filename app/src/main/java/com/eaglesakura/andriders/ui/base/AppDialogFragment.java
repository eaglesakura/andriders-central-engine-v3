package com.eaglesakura.andriders.ui.base;

import com.eaglesakura.android.framework.ui.dialog.DialogFragmentDelegate;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * アプリ内でのダイアログ管理を行う
 */
public abstract class AppDialogFragment extends AppBaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mDelegate.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mDelegate.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDelegate.onDestroy();
    }

    @Nullable
    public Dialog getDialog() {
        return mDelegate.getDialog();
    }

    private DialogFragmentDelegate mDelegate = new DialogFragmentDelegate(this, new DialogFragmentDelegate.InternalCallback() {
        @NonNull
        @Override
        public Dialog onCreateDialog(DialogFragmentDelegate self, Bundle savedInstanceState) {
            return AppDialogFragment.this.onCreateDialog(savedInstanceState);
        }

        @Override
        public void onDismiss(DialogFragmentDelegate self) {

        }
    });

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
