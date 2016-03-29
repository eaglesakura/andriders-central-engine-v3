package com.eaglesakura.andriders.ui.base;

import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.util.LogUtil;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

/**
 * アプリ内でのダイアログ管理を行う
 */
public abstract class AppDialogFragment extends AppBaseFragment {
    private Dialog mDialog;

    @Nullable
    public Dialog getDialog() {
        return mDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.widget("show dialog");
        mDialog = onCreateDialog(savedInstanceState);
        mDialog.setOnDismissListener(it -> {
            if (mDialog == null) {
                return;
            }
            AppLog.widget("Detach DialogFragment");
            detatchSelf(false);
        });
        mDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppLog.widget("suspend dialog");
        if (mDialog != null && mDialog.isShowing()) {
            Dialog dialog = mDialog;
            mDialog = null;
            dialog.dismiss();
        }
        super.onStop();
        AppLog.widget("remove dialog");
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
     *
     * @param fragmentManager 管理下とするFragmentManager
     */
    public void show(FragmentManager fragmentManager) {
        if (mDialog != null) {
            throw new IllegalStateException();
        }
        fragmentManager
                .beginTransaction()
                .add(this, createSimpleTag())
                .commit();
    }
}
