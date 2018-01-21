package com.eaglesakura.andriders.service.log;

import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.central.data.log.SessionLogger;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionState;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.provider.AppDatabaseProvider;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.sloth.app.lifecycle.ServiceLifecycle;
import com.squareup.otto.Subscribe;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

/**
 * セッションログの書き込み管理コントローラ
 */
public class SessionLogController {
    ServiceLifecycle mLifecycleDelegate = new ServiceLifecycle();

    SessionLogger mLogger;

    @Inject(value = AppDatabaseProvider.class, name = AppDatabaseProvider.NAME_WRITEABLE)
    SessionLogDatabase mDatabase;

    @NonNull
    final ClockTimer mCommitTimer;

    private SessionLogController(CentralSession centralSession) {
        mLifecycleDelegate.onCreate();
        mLogger = new SessionLogger(centralSession.getSessionInfo());
        mCommitTimer = new ClockTimer(centralSession.getSessionClock());
    }

    public static SessionLogController attach(ServiceLifecycle lifecycleDelegate, CentralSession session) {
        SessionLogController result = new SessionLogController(session);
        session.getStateBus().bind(lifecycleDelegate, result);
        session.getDataStream().observe(lifecycleDelegate, result::observeSessionData);

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
    @UiThread
    private void observeSessionData(RawCentralData data) {
        mLogger.onUpdate(data);
        if (mCommitTimer.overTimeMs(COMMIT_INTERVAL_TIME_SEC) && mLogger.hasPointCaches()) {
            commitAsync();
            mCommitTimer.start();
        }
    }

    private void commitAsync() {
        // コミット対象のキャッシュを持っていない
        if (!mLogger.hasPointCaches()) {
            return;
        }

        mLifecycleDelegate.async(ExecuteTarget.LocalQueue, CallbackTime.FireAndForget, task -> {
            try (SessionLogDatabase db = mDatabase.open(0x00)) {
                db.runInTx(() -> {
                    mLogger.commit(mDatabase);
                    return 0;
                });
            }

            // 同タイミングでキャッシュ削除のGCをかける
            System.gc();

            return this;
        }).start();
    }
}
