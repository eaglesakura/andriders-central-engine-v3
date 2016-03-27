package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.ui.navigation.log.dialog.GpxImportDialogFragment;

import android.support.annotation.UiThread;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * その他のファイルからログを読み込むためのFragment
 */
public class LogImportFragment extends AppBaseFragment {
    public LogImportFragment() {
        setHasOptionsMenu(true);
    }

    /**
     * GPXのインポートガイドを開始する
     */
    @UiThread
    void startImportGpx() {
        GpxImportDialogFragment dialog = new GpxImportDialogFragment();
        dialog.show(getChildFragmentManager());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_userlog_import, menu);
        menu.findItem(R.id.UserLog_Import_GPX).setOnMenuItemClickListener(item -> {
            startImportGpx();
            return true;
        });
    }
}
