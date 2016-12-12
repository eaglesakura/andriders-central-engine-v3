package com.eaglesakura.andriders.service.log;

import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.central.data.log.SessionLogger;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionData;
import com.eaglesakura.andriders.central.service.SessionState;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.provider.AppDatabaseProvider;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.squareup.otto.Subscribe;

import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

/**
 * セッションログの書き込み管理コントローラ
 */
public class SessionLogController {
    ServiceLifecycleDelegate mLifecycleDelegate = new ServiceLifecycleDelegate();

    SessionLogger mLogger;

    Set<OnCommitListener> mCommitListeners = new HashSet<>();

    @Inject(AppDatabaseProvider.class)
    SessionLogDatabase mDatabase;

    @NonNull
    final ClockTimer mCommitTimer;

    private SessionLogController(CentralSession centralSession) {
        mLifecycleDelegate.onCreate();
        mLogger = new SessionLogger(centralSession.getSessionInfo());
        mCommitTimer = new ClockTimer(centralSession.getSessionClock());
    }

    public void addListener(OnCommitListener listener) {
        mCommitListeners.add(listener);
    }

    public static SessionLogController attach(ServiceLifecycleDelegate lifecycleDelegate, CentralSession session) {
        SessionLogController result = new SessionLogController(session);
        session.getStateBus().bind(lifecycleDelegate, result);
        session.getDataBus().bind(lifecycleDelegate, result);

        Garnet.inject(result);

        return result;
    }

    /**
     * Sessionのステート変更通知をハンドリングする
     */
    @Subscribe
    private void onSessionStateChanged(SessionState.Bus state) {
        // 必要に応じてハンドリングを追加する
        AppLog.system("SessionState ID[%d] Changed[%s]", state.getSession().getSessionId(), state.getState());
        if (state.getState() == SessionState.State.Destroyed) {
            // 最後のステートの場合、強制的に更新する
            // 初期化が終わる前にDestroyが行われる可能性があるので注意する
            CentralDataManager centralDataManager = state.getSession().getCentralDataManager();
            if (centralDataManager != null && centralDataManager.getLatestCentralData() != null) {
                AppLog.db("finalize commit");
                mLogger.add(centralDataManager.getLatestCentralData());
                commitAsync();
            }
        }
    }

    /**
     * データコミットをかける時間インターバル
     */
    private static final long COMMIT_INTERVAL_TIME_SEC = 1000 * 5;

    /**
     * データが更新された
     */
    @Subscribe
    private void onSessionDataChanged(SessionData.Bus data) {
        mLogger.onUpdate(data.getLatestData());
        if (mCommitTimer.overTimeMs(COMMIT_INTERVAL_TIME_SEC) && mLogger.hasPointCaches()) {
            commitAsync();
            mCommitTimer.start();
        }
    }

    public interface OnCommitListener {
        /**
         * データのコミットが行われた場合に呼び出される
         */
        void onCommit(SessionLogController self);
    }

    private void commitAsync() {
        // コミット対象のキャッシュを持っていない
        if (!mLogger.hasPointCaches()) {
            return;
        }

        mLifecycleDelegate.async(ExecuteTarget.LocalQueue, CallbackTime.FireAndForget, task -> {
            try (SessionLogDatabase db = mDatabase.openWritable(SessionLogDatabase.class)) {
                db.runInTx(() -> {
                    mLogger.commit(mDatabase);
                    return 0;
                });
            }

            // 同タイミングでキャッシュ削除のGCをかける
            System.gc();

            return this;
        }).completed((result, task) -> {
            for (OnCommitListener listener : mCommitListeners) {
                listener.onCommit(this);
            }
        }).start();
    }
}
