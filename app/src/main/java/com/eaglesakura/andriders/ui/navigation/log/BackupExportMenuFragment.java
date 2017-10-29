package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.data.backup.CentralBackupExporter;
import com.eaglesakura.andriders.data.backup.serialize.SessionBackup;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.OnMenuClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.sloth.annotation.BindInterface;
import com.eaglesakura.sloth.annotation.FragmentMenu;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;
import com.eaglesakura.sloth.ui.progress.DialogToken;
import com.eaglesakura.sloth.ui.progress.ProgressToken;
import com.eaglesakura.sloth.view.builder.DialogBuilder;
import com.eaglesakura.util.StringUtil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.Date;

/**
 * 完全なログバックアップを行なう
 */
@FragmentMenu(R.menu.user_log_backup_export)
public class BackupExportMenuFragment extends AppFragment {

    @BindInterface
    Callback mCallback;


    @Inject(AppManagerProvider.class)
    CentralLogManager mCentralLogManager;

    /**
     * バックアップボタンを押した
     */
    @OnMenuClick(R.id.Menu_Export_Backup)
    void clickBackup() {
        AppDialogBuilder.newInformation(getContext(), R.string.Message_Log_ExportBackup)
                .positiveButton(R.string.Word_Common_OK, () -> {
                    AppLog.widget("Pick Output File");

                    long dateId = SessionHeader.toDateId(mCallback.getBackupTargetSessionId(this));

                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.setType("application/*");
                    intent.putExtra(Intent.EXTRA_TITLE, StringUtil.format("backup-%d%s", dateId, CentralBackupExporter.EXT_BACKUP_FILE));
                    startActivityForResult(intent, AppConstants.REQUEST_PICK_BACKUP_FILE);
                })
                .negativeButton(R.string.Word_Common_Cancel, null)
                .show(getFragmentLifecycle());
    }

    @OnActivityResult(AppConstants.REQUEST_PICK_BACKUP_FILE)
    void resultPickBackupFile(int result, Intent data) {
        if (result != Activity.RESULT_OK) {
            return;
        }

        AppLog.widget("Backup %s", data.getData().toString());
        // 設定画面を開く
        UIHandler.postUI(() -> {
            showExportDialog(data.getData());
        });
    }

    @UiThread
    void showExportDialog(Uri uri) {
        DialogBuilder builder = AppDialogBuilder.newProgress(getContext(), getString(R.string.Word_Common_DataWrite))
                .title(R.string.Title_Log_Backup)
                .cancelable(true)
                .canceledOnTouchOutside(false)
                .positiveButton(R.string.EsMaterial_Dialog_Cancel, null);
        DialogToken token = DialogBuilder.showAsToken(builder, getFragmentLifecycle());
        asyncQueue(task -> {
            try (DialogToken _token = token;
                 ProgressToken _token2 = pushProgress(R.string.Word_Common_Working)) {

                long sessionId = mCallback.getBackupTargetSessionId(this);
                mCentralLogManager.exportDailySessions(sessionId, new CentralLogManager.ExportCallback() {
                    @Override
                    public void onStart(CentralLogManager self, @NonNull SessionHeader header) {
                        token.updateContent(view -> {
                            Date date = new Date(header.getSessionId());
                            String text = getString(R.string.Message_Log_ExportSession, LogSummaryBinding.DEFAULT_DATE_FORMATTER.format(date));
                            new AQuery(view).id(R.id.EsMaterial_Progress_Text).text(text);
                        });
                    }

                    @Override
                    public void onStartCompress(CentralLogManager self, @NonNull SessionHeader session, SessionBackup backup) {

                    }
                }, uri, SupportCancelCallbackBuilder.from(task).or(token).build());
                return this;
            }
        }).canceled(task -> {
            AppLog.db("Cancel Backup");
        }).completed((result, task) -> {
            AppDialogBuilder.newInformation(getContext(), R.string.Message_Log_BackupCompleted)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getFragmentLifecycle());
        }).failed((error, task) -> {
            AppLog.printStackTrace(error);
            AppDialogBuilder.newError(getContext(), error)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getFragmentLifecycle());
        }).start();
    }

    public interface Callback {
        /**
         * 代表セッションIDを取得する
         */
        long getBackupTargetSessionId(BackupExportMenuFragment self);
    }
}
