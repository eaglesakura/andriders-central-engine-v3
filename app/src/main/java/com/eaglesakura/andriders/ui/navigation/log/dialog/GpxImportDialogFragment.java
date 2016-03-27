package com.eaglesakura.andriders.ui.navigation.log.dialog;

import com.eaglesakura.andriders.ui.base.AppDialogFragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

/**
 * GPXインポートを行う
 *
 * ファイル選択等で画面遷移が多く発生するため、Fragment化する
 */
public class GpxImportDialogFragment extends AppDialogFragment {
    @NonNull
    @Override
    protected Dialog onCreateDialog(Bundle state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("GPX読込");
        builder.setMessage("読み込みます");
        builder.setPositiveButton("OK", (button, which) -> {
            button.dismiss();
        });
        return builder.create();
    }
}
