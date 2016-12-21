package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.central.data.log.SessionHeaderCollection;
import com.eaglesakura.andriders.databinding.UserDailyLogSessionRowBinding;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.material.widget.support.SupportCancelCallbackBuilder;

import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 日次ログ表示Fragment
 */
@FragmentLayout(R.layout.user_daily_log)
public class DailyLogFragmentMain extends AppNavigationFragment {

    /**
     * 起点となるセッション
     *
     * このセッションを含む日が表示対象となる
     */
    @BundleState
    long mSampleSessionId;

    @Inject(AppManagerProvider.class)
    CentralLogManager mCentralLogManager;

    /**
     * 起点となるセッションを指定する
     */
    public void setSampleSessionId(long sampleSessionId) {
        mSampleSessionId = sampleSessionId;
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
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
        }).start();
    }

    @UiThread
    void onLoadHeaders(SessionHeaderCollection sessionHeaders) {

        if (sessionHeaders.isEmpty()) {
            mCallback.onSessionNotFound(this);
        } else {
            mLogAdapter.getCollection().add(null);  // 先頭はトータル値
            mLogAdapter.getCollection().addAll(sessionHeaders.listSessionDates().list());
        }
        mSessionDateList.setProgressVisibly(false, sessionHeaders.size());
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
            // 1アイテムをロード
            loadSession(bind);
        }
    };

    /**
     * 1アイテムを読み込み、カードへ反映する
     */
    @UiThread
    void loadSession(CardAdapter.CardBind<SessionHeader> bind) {
        async(ExecuteTarget.LocalParallel, CallbackTime.Foreground, (BackgroundTask<LogStatistics> task) -> {
            CancelCallback cancelCallback = SupportCancelCallbackBuilder.from(task).or(bind).build();
            try (ProgressToken token = pushProgress(R.string.Word_Common_DataLoad)) {
                return mCentralLogManager.loadSessionStatistics(bind.getItem(), cancelCallback);
            }
        }).completed((result, task) -> {
            UserDailyLogSessionRowBinding binding = bind.getBinding();
            binding.setItem(new LogSummaryBinding(getContext(), result));
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
    }
}
