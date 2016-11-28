package com.eaglesakura.andriders.service;

import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionState;
import com.eaglesakura.andriders.plugin.internal.CentralServiceCommand;
import com.eaglesakura.andriders.service.server.CentralSessionServer;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.android.util.ContextUtil;
import com.squareup.otto.Subscribe;

import org.greenrobot.greendao.annotation.NotNull;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

/**
 * Session管理Service
 */
public class CentralSessionService extends Service {

    /**
     * 現在実行中のセッション情報
     */
    @Nullable
    SessionContext mSession;

    /**
     * その他のプロセスと通信するためのコマンドサーバ
     */
    @NotNull
    final CentralSessionServer mSessionServer;

    ServiceLifecycleDelegate mServiceLifecycleDelegate = new ServiceLifecycleDelegate();

    public CentralSessionService() {
        mSessionServer = new CentralSessionServer(this, mSessionServerCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mSessionServer.getBinder(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceLifecycleDelegate.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String ACTION = intent.getAction();
            AppLog.system("onStartCommand action[%s]", ACTION);
            if (CentralServiceCommand.ACTION_SESSION_START.equals(ACTION)) {
                // セッションを開始させる
                if (mSession == null) {
                    startNewSession(intent);
                }
            } else if (CentralServiceCommand.ACTION_SESSION_STOP.equals(ACTION)) {
                stopCurrentSession(intent);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mServiceLifecycleDelegate.onDestroy();
        stopCurrentSession(null);
        super.onDestroy();
    }

    /**
     * 新規セッションを開始する
     */
    @UiThread
    protected void startNewSession(Intent intent) {
        SessionContext sessionContext = new SessionContext(this);
        sessionContext.initialize(intent);
        mSession = sessionContext;

    }

    /**
     * 現在のセッションを停止する
     */
    @UiThread
    protected void stopCurrentSession(@Nullable Intent intent) {
        if (mSession != null) {
            mSession.dispose();
            mSession = null;
        }
    }

    /**
     * Sessionのステート変更通知をハンドリングする
     */
    @Subscribe
    private void onSessionStateChanged(SessionState.Bus state) {
        // 必要に応じてハンドリングを追加する
        AppLog.system("SessionState ID[%d] Changed[%s]", state.getSession().getSessionId(), state.getState());
        if (state.getState() == SessionState.State.Running) {
            // 実行中にステートが変わったので、走行通知をする
            mSessionServer.notifyOnSessionStarted(state.getSession().getSessionInfo());
        } else if (state.getState() == SessionState.State.Destroyed) {
            mSessionServer.notifyOnSessionStopped(state.getSession().getSessionInfo());
        }
    }

    /**
     * セッション制御のコールバック
     */
    private final CentralSessionServer.Callback mSessionServerCallback = new CentralSessionServer.Callback() {
        @Override
        @Nullable
        public CentralSession getCurrentSession(CentralSessionServer self) {
            if (mSession != null) {
                return mSession.getSession();
            }
            return null;
        }
    };

    public static boolean isRunning(Context context) {
        return ContextUtil.isServiceRunning(context, CentralSessionService.class);
    }
}
