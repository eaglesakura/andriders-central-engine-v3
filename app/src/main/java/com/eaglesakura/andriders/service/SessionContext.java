package com.eaglesakura.andriders.service;

import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.plugin.internal.CentralServiceCommand;
import com.eaglesakura.andriders.service.log.SessionLogController;
import com.eaglesakura.andriders.service.ui.AnimationFrame;
import com.eaglesakura.andriders.service.ui.CentralDisplayWindow;
import com.eaglesakura.andriders.service.ui.CentralStatusBar;
import com.eaglesakura.andriders.service.ui.ServiceAnimationController;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;

import android.app.Service;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * サービスで動作させる1セッションの情報を管理する
 */
public class SessionContext {
    @NonNull
    private final Service mService;

    @NonNull
    private final ServiceLifecycleDelegate mLifecycleDelegate = new ServiceLifecycleDelegate();

    /**
     * 現在走行中のセッションデータ
     */
    @NonNull
    CentralSession mSession;

    /**
     * 走行中のログ保存管理
     */
    @NonNull
    SessionLogController mSessionLogController;

    /**
     * セッションのNotification通知管理
     *
     * CentralSessionと
     */
    @NonNull
    CentralStatusBar mSessionNotification;

    /**
     * アニメーション管理
     */
    @NonNull
    ServiceAnimationController mAnimationController;

    /**
     * アニメーション管理バス
     */
    @NonNull
    AnimationFrame.Bus mAnimationFrameBus = new AnimationFrame.Bus(new AnimationFrame());

    /**
     * 通知レンダリングエリア
     */
    @NonNull
    CentralDisplayWindow mCentralDisplayWindow;

    public SessionContext(@NonNull Service service) {
        mService = service;
    }

    /**
     * セッションを初期化する
     */
    public void initialize(Intent intent) {
        mLifecycleDelegate.onCreate();

        SessionInfo sessionInfo = new SessionInfo.Builder(mService, new Clock(System.currentTimeMillis()))
                .debuggable(intent.getBooleanExtra(CentralServiceCommand.EXTRA_BOOT_DEBUG_MODE, false))
                .build();

        CentralSession.InitializeOption option = new CentralSession.InitializeOption();

        CentralSession centralSession = CentralSession.newInstance(sessionInfo);
        centralSession.getStateBus().bind(mLifecycleDelegate, this);
        centralSession.getStateBus().bind(mLifecycleDelegate, mService);

        mSessionLogController = SessionLogController.attach(mLifecycleDelegate, centralSession);
        mSessionNotification = CentralStatusBar.attach(mLifecycleDelegate, centralSession, mNotificationCallback);
        mAnimationController = ServiceAnimationController.attach(mLifecycleDelegate, centralSession, mAnimationCallback);
        mCentralDisplayWindow = CentralDisplayWindow.attach(mService, mLifecycleDelegate, mAnimationFrameBus, centralSession);

        mLifecycleDelegate.asyncUI((BackgroundTask<CentralSession> task) -> {
            centralSession.initialize(option, AppSupportUtil.asCancelCallback(task));
            return centralSession;
        }).completed((result, task) -> {
            mSession = centralSession;
            // プラグインの接続を行う
            mSession.getPluginCollection().safeEach(plugin -> {
                plugin.setNotificationManager(() -> mCentralDisplayWindow.getCentralNotificationManager());
                plugin.setCentralDataManager(() -> mSession.getCentralDataManager());

                // 起動完了を通知
                plugin.onCentralBootCompleted();
            });
        }).failed((error, task) -> {
            AppLog.printStackTrace(error);
        }).start();
    }

    @NonNull
    public void dispose() {
        mLifecycleDelegate.async(ExecuteTarget.LocalQueue, CallbackTime.FireAndForget, task -> {
            if (mSession != null) {
                mSession.dispose();
            }
            return this;
        }).finalized(task -> {
            mLifecycleDelegate.onDestroy();
        }).start();
    }

    @NonNull
    public Service getService() {
        return mService;
    }

    @NonNull
    public SessionInfo getSessionInfo() {
        return getSession().getSessionInfo();
    }

    @Nullable
    public CentralSession getSession() {
        return mSession;
    }

    @Nullable
    public SessionLogController getSessionLogController() {
        return mSessionLogController;
    }

    @Nullable
    public CentralStatusBar getSessionNotification() {
        return mSessionNotification;
    }

    @Nullable
    public ServiceAnimationController getAnimationController() {
        return mAnimationController;
    }

    @Nullable
    public AnimationFrame.Bus getAnimationFrameBus() {
        return mAnimationFrameBus;
    }

    /**
     * 通知制御のコールバック
     */
    private final CentralStatusBar.Callback mNotificationCallback = new CentralStatusBar.Callback() {
        @Override
        public Service getService(CentralStatusBar self) {
            return mService;
        }

        @Override
        public void onClickNotification(CentralStatusBar self) {
            AppLog.system("Click Notification");
        }
    };

    private final ServiceAnimationController.Callback mAnimationCallback = new ServiceAnimationController.Callback() {
        /**
         * 前回のセッション更新からの経過時間
         */
        double mSessionUpdateDeltaSec;

        @Override
        public void onUpdate(ServiceAnimationController self, CentralSession session, double deltaSec) {
            mSessionUpdateDeltaSec += deltaSec;
            if (mSessionUpdateDeltaSec > 1.0) {
                // 1秒以上経過したのでセッション情報を行進
                mSession.onUpdate(mSessionUpdateDeltaSec);
                mSessionUpdateDeltaSec = 0;
            }

            // アニメーションを追加
            mAnimationFrameBus.onUpdate(session, deltaSec);
        }
    };
}
