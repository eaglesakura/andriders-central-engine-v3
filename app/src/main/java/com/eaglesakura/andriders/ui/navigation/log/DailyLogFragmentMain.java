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
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.sloth.annotation.BindInterface;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.app.FragmentHolder;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;
import com.eaglesakura.sloth.ui.progress.DialogToken;
import com.eaglesakura.sloth.ui.progress.ProgressToken;
import com.eaglesakura.sloth.view.adapter.CardAdapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
            = FragmentHolder.newInstance(this, GoogleFitUploadMenuFragment.class, 0);

    /**
     * 完全なバックアップメニュー
     */
    FragmentHolder<BackupExportMenuFragment> mBackupMenuFragment
            = FragmentHolder.newInstance(this, BackupExportMenuFragment.class, 0);

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
    RecyclerView mListView;

    @Override
    protected void onCreateLifecycle(FragmentLifecycle lifecycle) {
        super.onCreateLifecycle(lifecycle);
        mFitUploadMenuFragment.bind(lifecycle);
        mBackupMenuFragment.bind(lifecycle);
    }

    /**
     * 起点となるセッションを指定する
     */
    public void setSampleSessionId(long sampleSessionId) {
        mSampleSessionId = sampleSessionId;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mListView.setHasFixedSize(false);
        mListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mListView.setItemAnimator(new DefaultItemAnimator());
        mListView.setAdapter(mAdapter);
        return view;
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
        asyncQueue((BackgroundTask<SessionHeaderCollection> task) -> {
            try (ProgressToken token = pushProgress(R.string.Word_Common_DataLoad)) {
                SupportCancelCallbackBuilder.CancelChecker checker = SupportCancelCallbackBuilder.from(task).build();
                return mCentralLogManager.listDailyHeaders(mSampleSessionId, checker);
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
                .show(getFragmentLifecycle());
    }

    /**
     * 指定セッションを削除する
     */
    @UiThread
    void deleteSession(SessionHeader session) {
        mCallback.onSessionDeleteStart(this, session);
        asyncQueue(task -> {
            try (DialogToken token = showProgress(R.string.Word_Common_DataDelete)) {
                mCentralLogManager.delete(session);
                return this;
            }
        }).failed((error, task) -> {
            AppLog.report(error);
        }).finalized(task -> {
            AppDialogBuilder.newInformation(getContext(), R.string.Message_Log_SessionDeleted)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getFragmentLifecycle());
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
        getFragmentLifecycle().async(ExecuteTarget.LocalParallel, CallbackTime.Foreground, (BackgroundTask<LogStatistics> task) -> {
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
