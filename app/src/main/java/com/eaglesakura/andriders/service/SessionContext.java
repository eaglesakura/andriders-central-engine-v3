package com.eaglesakura.andriders.service;

import com.eaglesakura.andriders.AceSdkUtil;
import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.data.notification.CentralNotificationManager;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.command.CommandDataCollection;
import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.service.command.CentralCommandController;
import com.eaglesakura.andriders.service.command.ProximityFeedbackManager;
import com.eaglesakura.andriders.service.log.SessionLogController;
import com.eaglesakura.andriders.service.ui.CentralDisplayWindow;
import com.eaglesakura.andriders.service.ui.CentralStatusBar;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.thread.HandlerLoopController;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.lambda.Action1;
import com.eaglesakura.sloth.app.lifecycle.ServiceLifecycle;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;

import android.app.Service;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

/**
 * サービスで動作させる1セッションの情報を管理する
 */
public class SessionContext {
    @NonNull
    private final Service mService;

    /**
     * 1セッション管理用のライフサイクル
     *
     * セッション管理にかかわるすべての非同期処理は、このライフサイクルによって管理される
     */
    @NonNull
    private final ServiceLifecycle mLifecycle = new ServiceLifecycle();

    /**
     * 現在走行中のセッションデータ
     */
    CentralSession mSession;

    /**
     * 走行中のログ保存管理
     */
    SessionLogController mSessionLogController;

    /**
     * セッションのNotification通知管理
     */
    CentralStatusBar mSessionStatusbar;

    /**
     * 通知レンダリングエリア
     */
    CentralDisplayWindow mCentralDisplayWindow;

    /**
     * コマンド管理
     */
    CentralCommandController mCommandController;

    /**
     * CentralManagerの定時更新を制御する
     */
    @Nullable
    HandlerLoopController mLoopController;

    public SessionContext(@NonNull Service service) {
        mService = service;
    }

    /**
     * セッションを初期化する
     *
     * @param intent Service開始時に適用されたIntentがそのまま渡される
     */
    public void initialize(Intent intent) {
        mLifecycle.onCreate();

        SessionInfo sessionInfo = new SessionInfo.Builder(mService, new Clock(System.currentTimeMillis()))
                .build();

        CentralSession centralSession = CentralSession.newInstance(sessionInfo);
        centralSession.getDataStream().observe(mLifecycle, this::observeSessionData);

        mSessionLogController = new SessionLogController(centralSession, mLifecycle);
        mSessionStatusbar = new CentralStatusBar(getService(), mLifecycle, centralSession, mNotificationCallback);

        mCentralDisplayWindow = new CentralDisplayWindow(mService, mLifecycle, centralSession);
        mCentralDisplayWindow.getCentralNotificationManager().addListener(mNotificationShowingListener);  // リスナを登録し、表示タイミングで対応アプリに通知できるようにする

        mCommandController = new CentralCommandController(mService, mLifecycle, centralSession, mCommandCallback);

        mLifecycle.asyncQueue((BackgroundTask<CentralSession> task) -> {
            SupportCancelCallbackBuilder.CancelChecker checker = SupportCancelCallbackBuilder.from(task).build();
            centralSession.initialize(checker);
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


            mLoopController = new HandlerLoopController(UIHandler.getInstance(), mOnAnimationUpdate);
            mLoopController.setFrameRate(2.0);  // 毎秒2回程度の更新に抑える
            mLoopController.connect();
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
    }

    public void dispose() {
        if (mLoopController != null) {
            mLoopController.disconnect();
            mLoopController = null;
        }

        mLifecycle.async(ExecuteTarget.LocalQueue, CallbackTime.FireAndForget, task -> {
            if (mSession != null) {
                mSession.dispose();
            }
            return this;
        }).finalized(task -> {
            mLifecycle.onDestroy();
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
    @UiThread
    private void observeSessionData(RawCentralData raw) {
        if ((raw.centralStatus.date - mLastDataBroadcastTime) < 1000) {
            return;
        }
        mLastDataBroadcastTime = raw.centralStatus.date;

        AppLog.broadcast("RawCentralData date[%d]", raw.centralStatus.date);

        mLifecycle.async(ExecuteTarget.LocalQueue, CallbackTime.Alive, (BackgroundTask<byte[]> task) -> {
            return AceSdkUtil.serializeToByteArray(raw);
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
                    intent.putExtra(CentralDataReceiver.EXTRA_CENTRAL_DATA, AceSdkUtil.serializeToByteArray(centralData));
                }
                mService.sendBroadcast(intent);
            } catch (Exception e) {
                AppLog.report(e);
            }
        }
    };

    /**
     * 通知制御のコールバック
     */
    private final CentralStatusBar.Callback mNotificationCallback = new CentralStatusBar.Callback() {
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
     * 定時更新の処理
     */
    private final Action1<Double> mOnAnimationUpdate = new Action1<Double>() {
        @Override
        public void action(Double deltaSec) throws Exception {
            // セッションが初期化されていないなら無視する
            if (mSession == null) {
                return;
            }
            // セッション内部時刻とリアル時刻の差分を経過時間として進める
            double centralDeltaSec = (double) (System.currentTimeMillis() - mSession.getSessionClock().now()) / 1000.0;
            mSession.onUpdate(centralDeltaSec);
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
