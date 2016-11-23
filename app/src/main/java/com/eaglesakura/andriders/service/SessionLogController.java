package com.eaglesakura.andriders.service;

import com.eaglesakura.andriders.central.data.log.SessionLogger;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionData;
import com.eaglesakura.andriders.central.service.SessionState;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.provider.AppDatabaseProvider;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.squareup.otto.Subscribe;

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

    private SessionLogController(CentralSession centralSession) {
        mLifecycleDelegate.onCreate();
        mLogger = new SessionLogger(centralSession.getSessionInfo());
    }

    public void addListener(OnCommitListener listener) {
        mCommitListeners.add(listener);
    }

    public static SessionLogController attach(CentralSession session) {
        SessionLogController result = new SessionLogController(session);
        session.registerStateBus(result);
        session.registerDataBus(result);

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
            RawCentralData latest = state.getSession().getCentralDataManager().getLatestCentralData();
            if (latest != null) {
            }
        }
    }

    /**
     * データが更新された
     */
    @Subscribe
    private void onSessionDataChanged(SessionData.Bus data) {
        mLogger.onUpdate(data.getLatestData());
        if (mLogger.hasPointCaches()) {
            commitAsync();
        }
    }

    public interface OnCommitListener {
        /**
         * データのコミットが行われた場合に呼び出される
         */
        void onCommit(SessionLogController self);
    }

    private void commitAsync() {
        mLifecycleDelegate.async(ExecuteTarget.NewThread, CallbackTime.FireAndForget, task -> {
            try (SessionLogDatabase db = mDatabase.openWritable(SessionLogDatabase.class)) {
                db.runInTx(() -> {
                    mLogger.commit(mDatabase);
                    return 0;
                });
            }
            return this;
        }).completed((result, task) -> {
            for (OnCommitListener listener : mCommitListeners) {
                listener.onCommit(this);
            }
        }).start();
    }
}
