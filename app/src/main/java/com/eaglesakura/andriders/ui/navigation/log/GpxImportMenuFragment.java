package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentMenu;
import com.eaglesakura.android.margarine.OnMenuClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.UiThread;

/**
 * GPXファイル読み込み機能を構築する
 */
@FragmentMenu(R.menu.user_log_gpx)
public class GpxImportMenuFragment extends AppFragment {

    /**
     * GPXの読み込みを開始する
     */
    @OnMenuClick(R.id.Menu_Import_Gpx)
    void clickImportGpx() {
        AppDialogBuilder.newInformation(getContext(), "GPXファイルをインポートしますか？")
                .positiveButton(R.string.Word_Common_OK, () -> {
                    AppLog.widget("Pick GPX File");

                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/*");

                    startActivityForResult(intent, AppConstants.REQUEST_PICK_GPXFILE);
                })
                .negativeButton(R.string.Word_Common_Cancel, null)
                .show(mLifecycleDelegate);
    }

    /**
     * ファイルが選択された
     */
    @OnActivityResult(AppConstants.REQUEST_PICK_GPXFILE)
    void resultPickGpxFile(int result, Intent data) {
        if (result != Activity.RESULT_OK) {
            return;
        }

        AppLog.widget("GPX %s", data.getData().toString());
        // 設定画面を開く
        UIHandler.postUI(() -> {
            showImportSettingDialog(data.getData());
        });
    }

    /**
     * Import設定を開く
     *
     * @param gpxFileSource 選択されたGPXファイル
     */
    @UiThread
    void showImportSettingDialog(Uri gpxFileSource) {

    }
}
