package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.central.data.log.DateSessions;
import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.central.data.log.SessionHeaderCollection;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.databinding.UserLogDailyRowBinding;
import com.eaglesakura.andriders.databinding.UserLogTotalRowBinding;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.FragmentHolder;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.annotation.BindInterface;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.material.widget.support.SupportCancelCallbackBuilder;
import com.eaglesakura.material.widget.support.SupportRecyclerView;
import com.eaglesakura.util.CollectionUtil;

import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * ログ表示画面のメインFragment
 */
@FragmentLayout(R.layout.user_log)
public class TotalLogFragmentMain extends AppNavigationFragment implements SessionModifyListener, GpxImportMenuFragment.Callback {

    /**
     * メニュー
     */
    FragmentHolder<GpxImportMenuFragment> mGpxImportMenu = FragmentHolder.newInstance(this, GpxImportMenuFragment.class, 0).bind(mLifecycleDelegate);

    /**
     * セッション情報表示
     */
    @Bind(R.id.Content_List)
    SupportRecyclerView mSessionDateList;

    @Inject(AppManagerProvider.class)
    CentralLogManager mCentralLogManager;

    @BindInterface
    Callback mCallback;

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
        mSessionDateList.setAdapter(mLogAdapter, false);
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
                return mCentralLogManager.listAllHeaders(cancelCallback);
            }
        }).completed((result, task) -> {
            onLoadHeaders(result);
        }).failed((error, task) -> {
            AppLog.report(error);
            mCallback.onSessionLoadFailed(this, error);
        }).start();
    }

    @UiThread
    void onLoadHeaders(SessionHeaderCollection sessionHeaders) {

        if (sessionHeaders.isEmpty()) {
            mCallback.onSessionNotFound(this);
            mLogAdapter.getCollection().clear();
        } else {
            mLogAdapter.getCollection().clear();
            mLogAdapter.getCollection().add(null);  // 先頭はトータル値
            mLogAdapter.getCollection().addAll(sessionHeaders.listSessionDates().list());
            mLogAdapter.notifyDataSetChanged();
        }
        mSessionDateList.setProgressVisibly(false, sessionHeaders.size());
    }

    /**
     * Adapter
     */
    CardAdapter<DateSessions> mLogAdapter = new CardAdapter<DateSessions>() {
        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                // 一番上はTotal設定
                return UserLogTotalRowBinding.class.hashCode();
            } else {
                // それ以外は日次設定
                return UserLogDailyRowBinding.class.hashCode();
            }
        }


        @Override
        protected View onCreateCard(ViewGroup parent, int viewType) {
            if (viewType == UserLogTotalRowBinding.class.hashCode()) {
                return UserLogTotalRowBinding.inflate(LayoutInflater.from(getContext()), parent, false).getRoot();
            } else {
                return UserLogDailyRowBinding.inflate(LayoutInflater.from(getContext()), parent, false).getRoot();
            }
        }

        @Override
        protected void onBindCard(CardBind<DateSessions> bind, int position) {
            if (bind.getItem() == null) {
                // トータル設定
                UserLogTotalRowBinding binding = bind.getBinding();
                binding.setItem(new LogSummaryBinding(getContext(), null));
                loadTotalSessions(bind);
            } else {
                // 日次表示
                UserLogDailyRowBinding binding = bind.getBinding();
                binding.setItem(new LogSummaryBinding(getContext(), null));
                binding.Item.setOnClickListener(null);
                loadDailySessions(bind.getItem(), bind);
            }
        }
    };

    /**
     * 日次ログをロードする
     */
    @UiThread
    void loadDailySessions(DateSessions daily, CardAdapter.CardBind<DateSessions> bind) {
        async(ExecuteTarget.LocalParallel, CallbackTime.Foreground, (BackgroundTask<LogStatistics> task) -> {
            SupportCancelCallbackBuilder.CancelChecker cancelChecker = SupportCancelCallbackBuilder
                    .from(task)
                    .or(bind)
                    .build();
            return mCentralLogManager.loadDailyStatistics(daily.getStartTime(), cancelChecker);
        }).completed((result, task) -> {
            UserLogDailyRowBinding binding = bind.getBinding();
            binding.setItem(new LogSummaryBinding(getContext(), result));
            binding.Item.setOnClickListener(view -> {
                mCallback.requestShowDetail(this, bind.getItem());
            });
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
    }

    /**
     * トータルログをロードする
     */
    @UiThread
    void loadTotalSessions(CardAdapter.CardBind<DateSessions> bind) {
        async(ExecuteTarget.LocalParallel, CallbackTime.Foreground, (BackgroundTask<LogStatistics> task) -> {
            SupportCancelCallbackBuilder.CancelChecker cancelChecker = SupportCancelCallbackBuilder
                    .from(task)
                    .or(bind)
                    .build();
            return mCentralLogManager.loadAllStatistics(cancelChecker);
        }).completed((result, task) -> {
            UserLogTotalRowBinding binding = bind.getBinding();
            binding.setItem(new LogSummaryBinding(getContext(), result));
        }).failed((error, task) -> {
            AppLog.report(error);
            // エラーは捨てる
        }).start();
    }

    @Override
    public void onDeleteSession(List<Long> sessions) {
        if (CollectionUtil.isEmpty(sessions)) {
            return;
        }
        // 日をチェックする
        final long DATE_ID = SessionHeader.toDateId(sessions.get(0));
        DateSessions dateSessions = mLogAdapter.getCollection().find(it -> it != null && it.getDateId() == DATE_ID);
        if (dateSessions == null) {
            return;
        }

        // 関連セッションを削除する
        for (long sessionId : sessions) {
            dateSessions.remove(sessionId);
        }

        if (dateSessions.isEmpty()) {
            // セッションが全て消えてしまった
            mLogAdapter.getCollection().remove(dateSessions);
        } else {
            // まだ残ってるのでInvalidate
            mLogAdapter.notifyItemChanged(mLogAdapter.getCollection().indexOf(dateSessions));
        }
    }

    @Override
    public void onGpxImportCompleted(GpxImportMenuFragment self, DataCollection<SessionInfo> sessions) {
        // 再度ログをロードする
        loadAllLogs();
    }

    public interface Callback {
        /**
         * セッションが見つからなかった
         */
        void onSessionNotFound(TotalLogFragmentMain self);

        /**
         * セッション情報ロードに失敗した
         */
        void onSessionLoadFailed(TotalLogFragmentMain self, Throwable error);

        /**
         * 詳細閲覧のリクエストを行う
         *
         * @param sessions 閲覧対象
         */
        void requestShowDetail(TotalLogFragmentMain self, DateSessions sessions);
    }
}
