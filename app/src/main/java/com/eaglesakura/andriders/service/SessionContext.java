package com.eaglesakura.andriders.service;

import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.plugin.internal.CentralServiceCommand;
import com.eaglesakura.andriders.service.log.SessionLogController;
import com.eaglesakura.andriders.service.ui.AnimationFrame;
import com.eaglesakura.andriders.service.ui.CentralNotification;
import com.eaglesakura.andriders.service.ui.ServiceAnimationController;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.rx.BackgroundTask;

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

    /**
     * 現在走行中のセッションデータ
     */
    @Nullable
    CentralSession mSession;

    /**
     * 走行中のログ保存管理
     */
    @Nullable
    SessionLogController mSessionLogController;

    /**
     * セッションのNotification通知管理
     *
     * CentralSessionと
     */
    @Nullable
    CentralNotification mSessionNotification;

    /**
     * アニメーション管理
     */
    @Nullable
    ServiceAnimationController mAnimationController;

    /**
     * アニメーション管理バス
     */
    @Nullable
    AnimationFrame.Bus mAnimationFrameBus = new AnimationFrame.Bus(new AnimationFrame());

    public SessionContext(@NonNull Service service) {
        mService = service;
    }

    /**
     * セッションを初期化する
     */
    public void initialize(Intent intent) {
        SessionInfo sessionInfo = new SessionInfo.Builder(mService, new Clock(System.currentTimeMillis()))
                .debuggable(intent.getBooleanExtra(CentralServiceCommand.EXTRA_BOOT_DEBUG_MODE, false))
                .build();

        CentralSession.InitializeOption option = new CentralSession.InitializeOption();

        CentralSession centralSession = CentralSession.newInstance(sessionInfo);
        centralSession.registerStateBus(this);

        mSessionLogController = SessionLogController.attach(centralSession);
        mSessionNotification = CentralNotification.attach(centralSession, mNotificationCallback);
        mAnimationController = ServiceAnimationController.attach(centralSession, mAnimationCallback);

        centralSession.initialize(option);

        mSession = centralSession;
    }

    @NonNull
    public BackgroundTask dispose() {
        return getSession().dispose();
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
    public CentralNotification getSessionNotification() {
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
    private final CentralNotification.Callback mNotificationCallback = new CentralNotification.Callback() {
        @Override
        public Service getService(CentralNotification self) {
            return mService;
        }

        @Override
        public void onClickNotification(CentralNotification self) {
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
