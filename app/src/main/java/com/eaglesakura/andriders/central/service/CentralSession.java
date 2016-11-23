package com.eaglesakura.andriders.central.service;

import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.gen.prop.UserProfiles;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.CentralPluginCollection;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.BackgroundTaskBuilder;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.rx.ResultCollection;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.Timer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 1セッションを管理する。
 * これはCommandService経由でリモートからも扱えるようにする。
 */
public class CentralSession {

    @NonNull
    final SessionInfo mSessionInfo;

    final ServiceLifecycleDelegate mServiceLifecycleDelegate = new ServiceLifecycleDelegate();

    /**
     * 現在のセッションデータを管理する
     */
    @NonNull
    CentralDataManager mCentralDataManager;

    @NonNull
    CentralPluginCollection mPluginCollection;

    @Inject(AppManagerProvider.class)
    PluginDataManager mPluginDataManager;

    @Inject(AppManagerProvider.class)
    CentralLogManager mCentralLogManager;

    /**
     * 現在のステート
     */
    @NonNull
    SessionState.Bus mState = new SessionState.Bus(new SessionState(SessionState.State.Initializing, this));

    /**
     * セッション更新バス
     */
    @NonNull
    SessionData.Bus mDataBus = new SessionData.Bus();

    CentralSession(SessionInfo sessionInfo) {
        mSessionInfo = sessionInfo;
        mServiceLifecycleDelegate.onCreate();
    }

    /**
     * セッションの基本状態を取得する
     */
    @NonNull
    public SessionInfo getSessionInfo() {
        return mSessionInfo;
    }

    /**
     * StateBusに登録する
     */
    public void registerStateBus(Object receiver) {
        mState.bind(mServiceLifecycleDelegate, receiver);
    }

    /**
     * DataBusに登録する
     */
    public void registerDataBus(Object receiver) {
        mState.bind(mServiceLifecycleDelegate, receiver);
    }

    /**
     * State通知用のBusを取得する
     */
    @NonNull
    public SessionState.Bus getStateBus() {
        return mState;
    }

    /**
     * セッションのメインデータ通知用のBusを取得する
     */
    @NonNull
    public SessionData.Bus getDataBus() {
        return mDataBus;
    }

    public long getSessionId() {
        return mSessionInfo.getSessionId();
    }

    public Clock getSessionClock() {
        return mSessionInfo.getSessionClock();
    }

    /**
     * 初期化を開始させる
     *
     * @return 初期化タスク, awaitを行うことで同期的に終了を待てる
     */
    @NonNull
    public BackgroundTask initialize(@Nullable InitializeOption option) {
        // 現在の設定をDumpする
        return async((BackgroundTask<ResultCollection> task) -> {
            CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task);
            CentralPluginCollection pluginCollection;

            // 既存のログを読み込む
            LogStatistics allStatistics = mCentralLogManager.loadAllStatistics();
            LogStatistics todayStatistics = mCentralLogManager.loadTodayStatistics(getSessionClock().now());

            // Centralモードで接続する
            {
                pluginCollection = mPluginDataManager.listPlugins(PluginDataManager.PluginListingMode.Active, cancelCallback);
                CentralPlugin.ConnectOption connectOption = new CentralPlugin.ConnectOption();
                connectOption.centralConnection = true;
                pluginCollection.connect(connectOption, cancelCallback);
            }

            return new ResultCollection()
                    .put(new CentralDataManager(mSessionInfo, allStatistics, todayStatistics))
                    .put(pluginCollection)
                    .put("log_all", allStatistics)
                    .put("log_today", todayStatistics)
                    ;
        }).completed((result, task) -> {
            mCentralDataManager = result.as(CentralDataManager.class);
            mPluginCollection = result.as(CentralPluginCollection.class);

            // State切り替えを通知する
            mState.modified(new SessionState(SessionState.State.Running, this));
        }).start();
    }

    /**
     * 毎フレーム更新を行う
     */
    public void onUpdate(double deltaSec) {
        // 実行中以外のステートは無視する
        if (getStateBus().getState() == SessionState.State.Running) {
            return;
        }

        Timer timer = new Timer();
        try {
            // 内部時間を経過させる
            Clock sessionClock = mSessionInfo.getSessionClock();
            sessionClock.offset((long) (deltaSec * 1000));

            // 更新を行う
            if (mCentralDataManager.onUpdate()) {
                // 更新が行えたので、Busに通知を流す
                mDataBus.modified(new SessionData(mCentralDataManager.getLatestCentralData(), this));
            }
        } finally {
            if (timer.end() > (1000 / 60)) {
                // 1フレームを超えたら警告を出す
                AppLog.system("Central UpdateTime too long!! [%d ms]", timer.end());
            }
        }
    }

    /**
     * データ管理クラスを取得する
     */
    public CentralDataManager getCentralDataManager() {
        return mCentralDataManager;
    }

    /**
     * 削除を行う
     *
     * @return 終了タスク, awaitを行うことで終了待ちを明示できる
     */
    @NonNull
    public BackgroundTask dispose() {
        mState.modified(new SessionState(SessionState.State.Stopping, this));
        return async(ExecuteTarget.LocalQueue, CallbackTime.FireAndForget, task -> {
            if (mPluginCollection != null) {
                mPluginCollection.disconnect();
            }
            return this;
        }).finalized(task -> {
            mPluginCollection = null;
            mCentralDataManager = null;
            mState.modified(new SessionState(SessionState.State.Destroyed, this));

            // 処理はすべて終了
            mServiceLifecycleDelegate.onDestroy();
        }).start();
    }

    private <T> BackgroundTaskBuilder<T> async(BackgroundTask.Async<T> background) {
        return mServiceLifecycleDelegate.asyncUI(background);
    }

    private <T> BackgroundTaskBuilder<T> async(ExecuteTarget execute, CallbackTime time, BackgroundTask.Async<T> background) {
        return mServiceLifecycleDelegate.async(execute, time, background);
    }

    public static CentralSession newInstance(@NonNull SessionInfo info) {
        CentralSession result = new CentralSession(info);

        Garnet.create(result)
                .depend(Context.class, info.getContext())
                .depend(UserProfiles.class, info.getUserProfiles())
                .inject();

        return result;
    }

    public static class InitializeOption {
    }
}
