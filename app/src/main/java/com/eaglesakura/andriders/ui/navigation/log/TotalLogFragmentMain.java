package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.central.data.log.DateSessions;
import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.log.SessionHeaderCollection;
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
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.material.widget.support.SupportCancelCallbackBuilder;
import com.eaglesakura.material.widget.support.SupportRecyclerView;

import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * ログ表示画面のメインFragment
 */
@FragmentLayout(R.layout.user_log)
public class TotalLogFragmentMain extends AppNavigationFragment {

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
        } else {
            mLogAdapter.getCollection().add(null);  // 先頭はトータル値
            mLogAdapter.getCollection().addAll(sessionHeaders.listSessionDates().getSource());
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
            if (position == 0) {
                // トータル設定
                UserLogTotalRowBinding binding = bind.getBinding();
                binding.setItem(new LogSummaryBinding(getContext(), null));
            } else {
                // 日次表示
                UserLogDailyRowBinding binding = bind.getBinding();
                binding.setItem(new LogSummaryBinding(getContext(), null));
            }
        }
    };

    /**
     * 日次ログをロードする
     */
    @UiThread
    void loadDailySessions(DateSessions daily, CardAdapter.CardBind<DateSessions> bind) {
        asyncUI((BackgroundTask<LogStatistics> task) -> {
            SupportCancelCallbackBuilder.CancelChecker cancelChecker = SupportCancelCallbackBuilder
                    .from(task)
                    .or(bind)
                    .build();
            return mCentralLogManager.loadDailyStatistics(daily.getStartTime(), cancelChecker);
        }).completed((result, task) -> {
            UserLogDailyRowBinding binding = bind.getBinding();
            binding.setItem(new LogSummaryBinding(getContext(), result));
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
    }

    /**
     * トータルログをロードする
     */
    @UiThread
    void loadTotalSessions(CardAdapter.CardBind<DateSessions> bind) {

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
    }
}
