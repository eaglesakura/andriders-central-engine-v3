package com.eaglesakura.andriders.service;

import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionData;
import com.eaglesakura.andriders.data.notification.CentralNotificationManager;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.command.CommandDataCollection;
import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.service.command.CentralCommandController;
import com.eaglesakura.andriders.service.command.ProximityFeedbackManager;
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
import com.eaglesakura.util.SerializeUtil;
import com.squareup.otto.Subscribe;

import android.app.Service;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * サービスで動作させる1セッションの情報を管理する
 */
@SuppressWarnings("ALL")
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
    CentralStatusBar mSessionStatusbar;

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

    /**
     * コマンド管理
     */
    @NonNull
    CentralCommandController mCommandController;

    public SessionContext(@NonNull Service service) {
        mService = service;
    }

    /**
     * セッションを初期化する
     */
    public void initialize(Intent intent) {
        mLifecycleDelegate.onCreate();

        SessionInfo sessionInfo = new SessionInfo.Builder(mService, new Clock(System.currentTimeMillis()))
                .build();

        CentralSession.InitializeOption option = new CentralSession.InitializeOption();

        CentralSession centralSession = CentralSession.newInstance(sessionInfo);
        centralSession.getStateBus().bind(mLifecycleDelegate, this);
        centralSession.getDataBus().bind(mLifecycleDelegate, this);
        centralSession.getStateBus().bind(mLifecycleDelegate, mService);

        mSessionLogController = SessionLogController.attach(mLifecycleDelegate, centralSession);

        mSessionStatusbar = CentralStatusBar.attach(mLifecycleDelegate, centralSession, mNotificationCallback);

        mAnimationController = ServiceAnimationController.attach(mLifecycleDelegate, centralSession, mAnimationCallback);

        mCentralDisplayWindow = CentralDisplayWindow.attach(mService, mLifecycleDelegate, mAnimationFrameBus, centralSession);
        mCentralDisplayWindow.getCentralNotificationManager().addListener(mNotificationShowingListener);  // リスナを登録し、表示タイミングで対応アプリに通知できるようにする

        mCommandController = CentralCommandController.attach(mService, mLifecycleDelegate, mAnimationFrameBus, centralSession, mCommandCallback);

        mLifecycleDelegate.asyncUI((BackgroundTask<CentralSession> task) -> {
            centralSession.initialize(option, AppSupportUtil.asCancelCallback(task));
            return centralSession;
        }).completed((result, task) -> {
            mSession = centralSession;
            // プラグインの接続を行う
            mSession.getPluginCollection().safeEach(plugin -> {
                plugin.setNotificationManager(() -> mCentralDisplayWindow.getCentralNotificationManager());
                plugin.setDisplayBindManager(() -> mCentralDisplayWindow.getCentralDisplayBindManager());
                plugin.setCentralDataManager(() -> mSession.getCentralDataManager());

                // 起動完了を通知
                plugin.onCentralBootCompleted();
            });
        }).failed((error, task) -> {
            AppLog.printStackTrace(error);
        }).start();
    }

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
    public CentralStatusBar getSessionStatusbar() {
        return mSessionStatusbar;
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
     * 最後にデータをBroadcastした時刻
     */
    private long mLastDataBroadcastTime = 0;

    /**
     * データ更新をハンドリングする
     *
     * データの圧縮等、送出には20ms程度を要する。
     * UIスレッドを止めるには多少気になる時間なので、圧縮はasyncで行う。
     */
    @Subscribe
    private void onSessionDataChanged(SessionData.Bus data) {
        RawCentralData raw = data.getLatestData();
        if (raw == null) {
            AppLog.broadcast("RawCentralData == null");
            return;
        }

        if ((raw.centralStatus.date - mLastDataBroadcastTime) < 1000) {
            return;
        }
        mLastDataBroadcastTime = raw.centralStatus.date;

        AppLog.broadcast("RawCentralData date[%d]", raw.centralStatus.date);

        mLifecycleDelegate.async(ExecuteTarget.LocalQueue, CallbackTime.Alive, (BackgroundTask<byte[]> task) -> {
            return SerializeUtil.serializePublicFieldObject(raw, true);
        }).completed((result, task) -> {
            // 対応アプリに対してブロードキャストを行う
            Intent intent = new Intent();
            intent.setAction(CentralDataReceiver.ACTION_UPDATE_CENTRAL_DATA);
            intent.addCategory(CentralDataReceiver.INTENT_CATEGORY);
            intent.putExtra(CentralDataReceiver.EXTRA_CENTRAL_DATA, result);
            mService.sendBroadcast(intent);
        }).start();

    }

    private final CentralNotificationManager.OnNotificationShowingListener mNotificationShowingListener = new CentralNotificationManager.OnNotificationShowingListener() {
        @Override
        public void onNotificationShowing(CentralNotificationManager self, NotificationData data) {
            try {
                Intent intent = new Intent();
                intent.setAction(CentralDataReceiver.ACTION_RECEIVED_NOTIFICATION);
                intent.addCategory(CentralDataReceiver.INTENT_CATEGORY);
                intent.putExtra(CentralDataReceiver.EXTRA_NOTIFICATION_DATA, data.serialize());
                RawCentralData centralData = mSession.getCentralDataManager().getLatestCentralData();
                if (centralData != null) {
                    intent.putExtra(CentralDataReceiver.EXTRA_CENTRAL_DATA, SerializeUtil.serializePublicFieldObject(centralData, true));
                }
                mService.sendBroadcast(intent);
            } catch (Throwable e) {
                AppLog.report(e);
            }
        }
    };

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

        @Override
        public void onClickToggleDisplay(CentralStatusBar self) {
            AppLog.system("Click ToogleDisplay");
            mCentralDisplayWindow.toggleVisible();
        }
    };

    /**
     * アニメーションコントロール
     */
    private final ServiceAnimationController.Callback mAnimationCallback = new ServiceAnimationController.Callback() {
        private boolean mClockInitialSync;

        private double mSessionDeltaSec;

        @Override
        public void onUpdate(ServiceAnimationController self, CentralSession session, double deltaSec) {
            // セッションが初期化されていないなら無視する
            if (mSession == null) {
                return;
            }

            {
                // セッション内部時間と現実時間とのズレを補正する
                double centralDeltaSec = (double) (System.currentTimeMillis() - session.getSessionClock().now()) / 1000.0;
                mSessionDeltaSec += centralDeltaSec;
                if (mSessionDeltaSec > 0.5) {
                    // 毎秒2回程度のアップデートに抑える
                    mSession.onUpdate(centralDeltaSec);
                    mSessionDeltaSec = 0;
                }
            }

            // アニメーションを追加
            mAnimationFrameBus.onUpdate(session, deltaSec);
        }
    };

    /**
     * コマンド制御・起動
     */
    private final CentralCommandController.Callback mCommandCallback = new CentralCommandController.Callback() {

        @Override
        public void onCommandLoaded(CentralCommandController self, CommandDataCollection commands) {
            ProximityFeedbackManager proximityFeedbackManager = self.getProximityFeedbackManager();
            if (proximityFeedbackManager != null) {
                // 近接コマンドフィードバックが行えるので、リンクする
                mCentralDisplayWindow.getNotificationView().setProximityFeedbackManager(proximityFeedbackManager);
            }
        }

        @Override
        public void requestActivityCommand(CentralCommandController self, CommandData data, Intent commandIntent) {
            try {
                mService.startActivity(commandIntent);
            } catch (Exception e) {
                AppLog.printStackTrace(e);
            }
        }

        @Override
        public void requestBroadcastCommand(CentralCommandController self, CommandData data, Intent commandIntent) {
            try {
                mService.sendBroadcast(commandIntent);             // Brodacastを投げる
            } catch (Exception e) {
                AppLog.printStackTrace(e);
            }
        }

        @Override
        public void requestServiceCommand(CentralCommandController self, CommandData data, Intent commandIntent) {
            try {
                mService.startService(commandIntent);  // Serviceを開始
            } catch (Exception e) {
                AppLog.printStackTrace(e);
            }
        }
    };
}
