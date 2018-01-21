package com.eaglesakura.andriders.service.log;

import com.eaglesakura.andriders.central.data.log.SessionLogger;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
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
import com.eaglesakura.sloth.app.lifecycle.Lifecycle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

/**
 * セッションログの書き込み管理コントローラ
 */
public class SessionLogController {
    @NonNull
    private Lifecycle mLifecycle;

    @NonNull
    private final SessionLogger mLogger;

    @NonNull
    private final SessionInfo mSessionInfo;

    /**
     * 最後に取得したCentralData
     */
    @Nullable
    private RawCentralData mLatestObserveCentralData;

    @Inject(value = AppDatabaseProvider.class, name = AppDatabaseProvider.NAME_WRITEABLE)
    SessionLogDatabase mDatabase;

    @NonNull
    final ClockTimer mCommitTimer;

    public SessionLogController(@NonNull CentralSession centralSession, @NonNull Lifecycle lifecycle) {
        mLifecycle = lifecycle;
        mSessionInfo = centralSession.getSessionInfo();
        mLogger = new SessionLogger(centralSession.getSessionInfo());
        mCommitTimer = new ClockTimer(centralSession.getSessionClock());

        centralSession.getStateStream().observe(lifecycle, this::observeSessionState);
        centralSession.getDataStream().observe(lifecycle, this::observeSessionData);

        Garnet.inject(this);
    }

    /**
     * Sessionのステート変更通知をハンドリングする
     */
    @UiThread
    private void observeSessionState(SessionState state) {
        // 必要に応じてハンドリングを追加する
        AppLog.system("SessionState ID[%d] Changed[%s]", mSessionInfo.getSessionId(), state.getState());
        if (state.getState() == SessionState.State.Destroyed) {
            // 最後のステートの場合、強制的に更新する
            // 初期化が終わる前にDestroyが行われる可能性があるので注意する
            if (mLatestObserveCentralData != null) {
                AppLog.db("finalize commit");
                mLogger.add(mLatestObserveCentralData);
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
        mLatestObserveCentralData = data;
    }

    private void commitAsync() {
        // コミット対象のキャッシュを持っていない
        if (!mLogger.hasPointCaches()) {
            return;
        }

        mLifecycle.async(ExecuteTarget.GlobalParallel, CallbackTime.FireAndForget, task -> {
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
