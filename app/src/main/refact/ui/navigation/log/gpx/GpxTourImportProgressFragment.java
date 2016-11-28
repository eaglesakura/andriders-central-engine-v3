package com.eaglesakura.andriders.ui.navigation.log.gpx;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.data.gpx.GpxParser;
import com.eaglesakura.andriders.data.importer.GpxImporter;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.util.AppLog;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

public class GpxTourImportProgressFragment extends AppBaseFragment {

    Listener mListener;

    public GpxTourImportProgressFragment() {
        mFragmentDelegate.setLayoutId(R.layout.gpx_import_setup);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = getParentOrThrow(Listener.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 読込を開始する
     */
    @UiThread
    public void startImport() {
        Uri src = mListener.getGpxFilePath(this);
        asyncUI((task) -> {
            GpxImporter gpxImporter = new GpxImporter(getContext(), src);
            gpxImporter.getParser().setDateOption(GpxParser.DateOption.AddTimeZone);

            // GPXのインストールを行う
            gpxImporter.install(() -> task.isCanceled());

            // TODO UIに反映
            AppLog.system("GPX Import Completed");
            return this;
        }).completed((result, task) -> {
            onSuccess();
        }).failed((error, task) -> {
            error.printStackTrace();
            onError(error);
        }).start();
        mListener.onImportStart(this);
    }

    @UiThread
    void onSuccess() {
        mListener.onImportComplete(this);
    }

    @UiThread
    void onError(Throwable e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("失敗");
        builder.setMessage("インポートに失敗しました。別なファイルを選択してください。");
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.Common_OK, (dig, which) -> {
            mListener.onImportFailed(this);
        });
        builder.show();
    }

    public interface Listener {

        @NonNull
        Uri getGpxFilePath(GpxTourImportProgressFragment self);

        /**
         * インポートを開始した
         */
        @NonNull
        void onImportStart(GpxTourImportProgressFragment self);

        /**
         * インポートが完了した
         */
        void onImportComplete(GpxTourImportProgressFragment self);

        /**
         * インポートが失敗した
         */
        void onImportFailed(GpxTourImportProgressFragment self);
    }
}
