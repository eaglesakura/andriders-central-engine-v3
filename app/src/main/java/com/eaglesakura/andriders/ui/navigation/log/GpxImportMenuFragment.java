package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.data.gpx.GpxParser;
import com.eaglesakura.andriders.data.importer.GpxImporter;
import com.eaglesakura.andriders.data.importer.SessionImportCommitter;
import com.eaglesakura.andriders.data.importer.SessionImporter;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.annotation.BindInterface;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentMenu;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.margarine.OnMenuClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.lambda.CancelCallback;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * GPXファイル読み込み機能を構築する
 */
@FragmentMenu(R.menu.user_log_gpx)
public class GpxImportMenuFragment extends AppFragment {

    @BindInterface
    Callback mCallback;

    /**
     * GPXの読み込みを開始する
     */
    @OnMenuClick(R.id.Menu_Import_Gpx)
    void clickImportGpx() {
        AppDialogBuilder.newInformation(getContext(), R.string.Message_Log_ImportGpx)
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
        View content = LayoutInflater.from(getContext()).inflate(R.layout.user_log_gpx_options, null, false);
        AppDialogBuilder.newCustomContent(getContext(), "時差補正方法を選択してください", content)
                .positiveButton(R.string.Word_Common_Import, () -> {
                    GpxImporter.Builder builder = new GpxImporter.Builder(getContext());
                    builder.uri(gpxFileSource);
                    int checkedRadioButtonId = new AQuery(content).id(R.id.Selector_TimeOffset).getView(RadioGroup.class).getCheckedRadioButtonId();
                    switch (checkedRadioButtonId) {
                        case R.id.Item_TimeAdd:
                            builder.parser(GpxParser.DateOption.AddTimeZone);
                            break;
                        case R.id.Item_TimeSub:
                            builder.parser(GpxParser.DateOption.SubTimeZone);
                            break;
                        default:
                            builder.parser(GpxParser.DateOption.None);
                            break;
                    }

                    startGpxFileImport(builder.build());
                })
                .negativeButton(R.string.Word_Common_Cancel, null)
                .show(mLifecycleDelegate);
    }

    /**
     * GPXファイルの読み込みを行う
     */
    @UiThread
    void startGpxFileImport(GpxImporter gpxImporter) {
        asyncUI((BackgroundTask<DataCollection<SessionInfo>> task) -> {
            CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task);
            List<SessionInfo> result = new ArrayList<SessionInfo>();
            try (ProgressToken token = pushProgress(R.string.Word_Common_DataLoad)) {
                SessionImportCommitter committer = new SessionImportCommitter(getContext()) {
                    @Override
                    public void onSessionFinished(SessionImporter self, CentralDataManager dataManager) throws AppException {
                        super.onSessionFinished(self, dataManager);
                        result.add(dataManager.getSessionInfo());
                        AppLog.db("Import Session[%d]", dataManager.getSessionId());
                    }
                };
                try (SessionLogDatabase db = committer.openDatabase()) {
                    db.runInTx(() -> {
                        gpxImporter.install(committer, cancelCallback);
                        return 0;
                    });
                }

                return new DataCollection<>(result);
            }
        }).failed((error, task) -> {
            AppLog.report(error);
        }).completed((result, task) -> {
            AppLog.db("Completed :: %d sessions", result.size());
            mCallback.onGpxImportCompleted(this, result);
        }).start();
    }

    public interface Callback {
        /**
         * セッションが読み込み終わった
         */
        void onGpxImportCompleted(GpxImportMenuFragment self, DataCollection<SessionInfo> sessions);
    }
}
