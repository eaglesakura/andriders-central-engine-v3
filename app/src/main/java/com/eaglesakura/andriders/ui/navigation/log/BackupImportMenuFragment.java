package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
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
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.sloth.annotation.BindInterface;
import com.eaglesakura.sloth.annotation.FragmentMenu;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;
import com.eaglesakura.sloth.ui.progress.DialogToken;
import com.eaglesakura.sloth.view.builder.DialogBuilder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.Date;

/**
 * バックアップからのファイル復元を行なう
 */
@FragmentMenu(R.menu.user_log_backup_import)
public class BackupImportMenuFragment extends AppFragment {

    @BindInterface
    Callback mCallback;


    @Inject(AppManagerProvider.class)
    CentralLogManager mCentralLogManager;

    /**
     * バックアップボタンを押した
     */
    @OnMenuClick(R.id.Menu_Import_Backup)
    void clickBackup() {
        AppDialogBuilder.newInformation(getContext(), R.string.Message_Log_ImportBackup)
                .positiveButton(R.string.Word_Common_OK, () -> {
                    AppLog.widget("Pick Output File");
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/*");
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
            showImportDialog(data.getData());
        });
    }

    @UiThread
    void showImportDialog(Uri uri) {
        DialogBuilder builder = AppDialogBuilder.newProgress(getContext(), getString(R.string.Word_Common_DataLoad))
                .title(R.string.Title_Log_Restore)
                .cancelable(true)
                .canceledOnTouchOutside(false)
                .positiveButton(R.string.EsMaterial_Dialog_Cancel, null);

        // UIスレッドで先行してダイアログを表示する
        // これは非同期処理開始前にFragmentが閉じられるのを防ぐため
        DialogToken token = DialogBuilder.showAsToken(builder, getFragmentLifecycle());

        asyncQueue((BackgroundTask<DataCollection<SessionHeader>> task) -> {
            try (DialogToken _token1 = token) {
                return mCentralLogManager.importFromBackup(new CentralLogManager.ImportCallback() {
                    @Override
                    public void onInsertStart(CentralLogManager self, @NonNull SessionBackup backup) {
                        token.updateContent(view -> {
                            SessionHeader header = SessionBackup.getSessionHeader(backup);
                            Date date = new Date(header.getSessionId());
                            String text = getString(R.string.Message_Log_ImportSession, LogSummaryBinding.DEFAULT_DATE_FORMATTER.format(date));
                            new AQuery(view).id(R.id.EsMaterial_Progress_Text).text(text);
                        });
                    }
                }, uri, SupportCancelCallbackBuilder.from(task).or(token).build());
            }
        }).canceled(task -> {
            AppLog.db("Cancel Restore");
        }).completed((result, task) -> {
            mCallback.onSessionImportCompleted(this, result);
        }).failed((error, task) -> {
            AppLog.printStackTrace(error);
            AppDialogBuilder.newError(getContext(), error)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getFragmentLifecycle());
        }).start();
    }

    public interface Callback {
        /**
         * セッションをインポート完了した
         */
        void onSessionImportCompleted(BackupImportMenuFragment self, DataCollection<SessionHeader> headers);
    }
}
