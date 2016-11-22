package com.eaglesakura.andriders.service;

import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionState;
import com.eaglesakura.andriders.plugin.internal.CentralServiceCommand;
import com.eaglesakura.andriders.service.server.SessionServer;
import com.eaglesakura.andriders.service.ui.SessionNotification;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
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
 *
 */
public class CentralSessionService extends Service {

    @Nullable
    CentralSession mSession;

    @NotNull
    final SessionServer mSessionServer;

    /**
     * セッション通知
     */
    SessionNotification mSessionNotification;

    ServiceLifecycleDelegate mServiceLifecycleDelegate = new ServiceLifecycleDelegate();

    public CentralSessionService() {
        mSessionServer = new SessionServer(this, mSessionServerCallback);
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
                    mSession = startNewSession(intent);
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
    protected CentralSession startNewSession(Intent intent) {
        SessionInfo sessionInfo = new SessionInfo.Builder(this, new Clock(System.currentTimeMillis()))
                .debuggable(intent.getBooleanExtra(CentralServiceCommand.EXTRA_BOOT_DEBUG_MODE, false))
                .build();

        CentralSession.InitializeOption option = new CentralSession.InitializeOption();

        CentralSession centralSession = CentralSession.newInstance(sessionInfo);
        centralSession.registerStateBus(this);
        centralSession.initialize(option);

        // Foreground Serviceとして起動する
        mSessionNotification = new SessionNotification(this, mNotificationCallback);
        mSessionNotification.onStartSession(centralSession);

        mServiceLifecycleDelegate.asyncUI(task -> {
            // 開始通知を送る
            mSessionServer.notifyOnSessionStarted(sessionInfo);
            return this;
        }).start();

        return centralSession;
    }

    /**
     * 現在のセッションを停止する
     */
    @UiThread
    protected void stopCurrentSession(@Nullable Intent intent) {
        if (mSessionNotification != null) {
            mSessionNotification.onStopSession(mSession);
            mSessionNotification = null;
        }

        if (mSession != null) {
            SessionInfo info = mSession.getSessionInfo();

            mSession.dispose();
            mSession = null;

            mServiceLifecycleDelegate.asyncUI(task -> {
                // 終了通知を送る
                mSessionServer.notifyOnSessionStopped(info);
                return this;
            }).start();
        }
    }

    /**
     * セッション制御のコールバック
     */
    private SessionServer.Callback mSessionServerCallback = new SessionServer.Callback() {
        @Override
        @Nullable
        public CentralSession getCurrentSession(SessionServer self) {
            return mSession;
        }
    };

    /**
     * 通知制御のコールバック
     */
    private SessionNotification.Callback mNotificationCallback = new SessionNotification.Callback() {
        @Override
        public void onClickNotification(SessionNotification self) {
            AppLog.system("Click Notification");
        }
    };

    public static boolean isRunning(Context context) {
        return ContextUtil.isServiceRunning(context, CentralSessionService.class);
    }

    /**
     * Sessionのステート変更通知をハンドリングする
     */
    @Subscribe
    void onSessionStateChanged(SessionState.Bus state) {
        if (state.getSession() != mSession) {
            // ハンドリング対象のステートなので、これはdropする
            return;
        }

        // 必要に応じてハンドリングを追加する
        AppLog.system("SessionState ID[%d] Changed[%s]", state.getSession().getSessionId(), state.getState());
    }
}
