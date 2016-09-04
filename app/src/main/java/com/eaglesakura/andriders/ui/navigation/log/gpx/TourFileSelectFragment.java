package com.eaglesakura.andriders.ui.navigation.log.gpx;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

public class TourFileSelectFragment extends AppBaseFragment {

    Listener mListener;

    public TourFileSelectFragment() {
        mFragmentDelegate.setLayoutId(R.layout.gpx_import_setup_filepick);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = getParentOrThrow(Listener.class);
    }

    @OnClick(R.id.Tour_File_Pick)
    void clickFilePickButton() {
        AppLog.widget("Pick GPX File");
        try {

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/*");

            startActivityForResult(intent, AppConstants.REQUEST_PICK_GPXFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnActivityResult(AppConstants.REQUEST_PICK_GPXFILE)
    void resultFilePick(int result, Intent data) {
        if (result != Activity.RESULT_OK) {
            return;
        }

        AppLog.widget("GPX %s -> %s", data.getDataString(), data.getData().toString());
        // タイミングをずらして発火する
        UIHandler.postUI(() -> {
            mListener.onSelectedFile(this, data.getData());
        });
    }


    public interface Listener {
        /**
         * ファイルが選択された
         */
        void onSelectedFile(TourFileSelectFragment self, @NonNull Uri path);
    }
}
