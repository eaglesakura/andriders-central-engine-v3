package com.eaglesakura.andriders.ui.navigation.log.dialog;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppDialogFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.material.widget.MaterialAlertDialog;
import com.eaglesakura.material.widget.MaterialDialogBase;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * GPXインポートを行う
 *
 * ファイル選択等で画面遷移が多く発生するため、Fragment化する
 */
public class GpxImportDialogFragment extends AppDialogFragment {
    @Bind(R.id.UserLog_Import_GPX_Path)
    EditText mGpxPath;

    @Bind(R.id.UserLog_Import_GPX_TimeZone)
    Spinner mTimeZoneSetting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestRuntimePermission(PermissionUtil.PermissionType.ExternalStorage);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!PermissionUtil.isRuntimePermissionGranted(getContext(), permissions)) {
            getDialog().dismiss();
        }
    }

    @NonNull
    @Override
    protected Dialog onCreateDialog(Bundle state) {
        MaterialAlertDialog dialog = new MaterialAlertDialog(getContext());
        dialog.setDialogContent(R.layout.dialog_import_gpx);
        dialog.setTitle("GPX読込");
        dialog.setPositiveButton("OK", (button, which) -> {
            button.dismiss();
        });

        MargarineKnife.from(dialog).to(this).bind();
        return dialog;
    }

    /**
     * GPXファイルが選択された
     *
     * TODO: Doc Pick Dialogを開く
     */
    @OnClick(R.id.UserLog_Import_GPX_Pick)
    void clickPickGpxFile() {
        AppLog.widget("Pick GPX File");
    }
}
