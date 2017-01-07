package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.central.data.log.SessionHeaderCollection;
import com.eaglesakura.andriders.databinding.UserDailyLogSessionRowBinding;
import com.eaglesakura.andriders.error.io.AppDataNotFoundException;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.FragmentHolder;
import com.eaglesakura.android.framework.ui.progress.DialogToken;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.annotation.BindInterface;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.material.widget.support.SupportCancelCallbackBuilder;
import com.eaglesakura.material.widget.support.SupportRecyclerView;

import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

/**
 * 日次ログ表示Fragment
 */
@FragmentLayout(R.layout.user_daily_log)
public class DailyLogFragmentMain extends AppNavigationFragment
        implements GoogleFitUploadMenuFragment.Callback, BackupExportMenuFragment.Callback {

    /**
     * Google Fitアップロードメニュー
     */
    FragmentHolder<GoogleFitUploadMenuFragment> mFitUploadMenuFragment
            = FragmentHolder.newInstance(this, GoogleFitUploadMenuFragment.class, 0).bind(mLifecycleDelegate);

    /**
     * 完全なバックアップメニュー
     */
    FragmentHolder<BackupExportMenuFragment> mBackupMenuFragment
            = FragmentHolder.newInstance(this, BackupExportMenuFragment.class, 0).bind(mLifecycleDelegate);

    /**
     * 起点となるセッション
     *
     * このセッションを含む日が表示対象となる
     */
    @BundleState
    long mSampleSessionId;

    @Inject(AppManagerProvider.class)
    CentralLogManager mCentralLogManager;

    @BindInterface
    Callback mCallback;

    @Bind(R.id.Content_List)
    SupportRecyclerView mListView;

    /**
     * 起点となるセッションを指定する
     */
    public void setSampleSessionId(long sampleSessionId) {
        mSampleSessionId = sampleSessionId;
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
        mListView.setAdapter(mAdapter, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadAllLogs();
    }

    /**
     * すべてのデータを読み込む
     */
    @UiThread
    void loadAllLogs() {
        asyncUI((BackgroundTask<SessionHeaderCollection> task) -> {
            try (ProgressToken token = pushProgress(R.string.Word_Common_DataLoad)) {
                CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task);
                return mCentralLogManager.listDailyHeaders(mSampleSessionId, cancelCallback);
            }
        }).completed((result, task) -> {
            onLoadHeaders(result);
        }).failed((error, task) -> {
            AppLog.report(error);
            mCallback.onLogLoadFailed(this, error);
        }).start();
    }

    @UiThread
    void onLoadHeaders(SessionHeaderCollection sessionHeaders) {
        if (sessionHeaders.isEmpty()) {
            mCallback.onLogLoadFailed(this, new AppDataNotFoundException());
        } else {
            mAdapter.getCollection().addAll(sessionHeaders.list());
        }
        mListView.setProgressVisibly(false, sessionHeaders.size());
    }


    CardAdapter<SessionHeader> mAdapter = new CardAdapter<SessionHeader>() {
        @Override
        protected View onCreateCard(ViewGroup parent, int viewType) {
            return UserDailyLogSessionRowBinding.inflate(LayoutInflater.from(getContext()), parent, false).getRoot();
        }

        @Override
        protected void onBindCard(CardBind<SessionHeader> bind, int position) {
            UserDailyLogSessionRowBinding binding = bind.getBinding();
            binding.setItem(new LogSummaryBinding(getContext(), null));
            binding.Item.setOnClickListener(view -> clickSession(bind.getItem()));
            // 1アイテムをロード
            loadSession(bind);
        }
    };

    /**
     * セッションカードをクリックした
     */
    @UiThread
    void clickSession(SessionHeader session) {
        AppDialogBuilder.newAlert(getContext(), R.string.Message_Log_DeleteThisSession)
                .title(LogSummaryBinding.DEFAULT_TIME_FORMATTER.format(new Date(session.getSessionId())))
                .positiveButton(R.string.Word_Common_Delete, () -> {
                    deleteSession(session);
                })
                .negativeButton(R.string.Word_Common_Cancel, null)
                .show(mLifecycleDelegate);
    }

    /**
     * 指定セッションを削除する
     */
    @UiThread
    void deleteSession(SessionHeader session) {
        mCallback.onSessionDeleteStart(this, session);
        asyncUI(task -> {
            try (DialogToken token = showProgress(R.string.Word_Common_DataDelete)) {
                mCentralLogManager.delete(session);
                return this;
            }
        }).failed((error, task) -> {
            AppLog.report(error);
        }).finalized(task -> {
            AppDialogBuilder.newInformation(getContext(), R.string.Message_Log_SessionDeleted)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(mLifecycleDelegate);
            mAdapter.getCollection().remove(session);

            // 親に動作を伝える
            mCallback.onSessionDeleted(this, session);
            if (mAdapter.getCollection().isEmpty()) {
                mCallback.onAllSessionDeleted(this);
            }
        }).start();
    }

    /**
     * 1アイテムを読み込み、カードへ反映する
     */
    @UiThread
    void loadSession(CardAdapter.CardBind<SessionHeader> bind) {
        async(ExecuteTarget.LocalParallel, CallbackTime.Foreground, (BackgroundTask<LogStatistics> task) -> {
            CancelCallback cancelCallback = SupportCancelCallbackBuilder.from(task).or(bind).build();
            return mCentralLogManager.loadSessionStatistics(bind.getItem(), cancelCallback);
        }).completed((result, task) -> {
            UserDailyLogSessionRowBinding binding = bind.getBinding();
            binding.setItem(new LogSummaryBinding(getContext(), result));
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
    }

    @Override
    public long getUploadTargetSessionId(GoogleFitUploadMenuFragment self) {
        return mSampleSessionId;
    }

    @Override
    public long getBackupTargetSessionId(BackupExportMenuFragment self) {
        return mSampleSessionId;
    }

    public interface Callback {
        /**
         * セッションを削除した
         */
        void onSessionDeleteStart(DailyLogFragmentMain self, SessionHeader header);

        /**
         * セッションを削除した
         */
        void onSessionDeleted(DailyLogFragmentMain self, SessionHeader header);

        /**
         * 表示可能なセッションがすべて削除された
         */
        void onAllSessionDeleted(DailyLogFragmentMain self);

        /**
         * ロードに失敗した
         */
        void onLogLoadFailed(DailyLogFragmentMain self, Throwable error);
    }
}
