package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.ui.navigation.log.dialog.GpxImportDialogFragment;
import com.eaglesakura.android.margarine.OnMenuClick;

import android.support.annotation.UiThread;

/**
 * その他のファイルからログを読み込むためのFragment
 */
public class LogImportFragment extends AppBaseFragment {
    public LogImportFragment() {
        requestOptionMenu(R.menu.fragment_userlog_import);
    }

    /**
     * GPXのインポートガイドを開始する
     */
    @UiThread
    void startImportGpx() {
        GpxImportDialogFragment dialog = new GpxImportDialogFragment();
        dialog.show(this);
    }

    /**
     * "GPXインポート"が選択された
     */
    @OnMenuClick(R.id.UserLog_Import_GPX)
    void clickMenuImportGpx() {
        startImportGpx();
    }
}
