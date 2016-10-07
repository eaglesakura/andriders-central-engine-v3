package com.eaglesakura.andriders.central.service;

import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
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
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.collection.AnonymousBroadcaster;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.Timer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

/**
 * 1セッションを管理する。
 * これはCommandService経由でリモートからも扱えるようにする。
 */
public class CentralSession {

    @NonNull
    final SessionInfo mSessionInfo;

    ServiceLifecycleDelegate mServiceLifecycleDelegate;

    /**
     * 現在のセッションデータを管理する
     */
    CentralDataManager mCentralDataManager;

    @Inject(AppManagerProvider.class)
    PluginDataManager mPluginDataManager;

    @Inject(AppManagerProvider.class)
    CentralLogManager mCentralLogManager;

    CentralPluginCollection mPluginCollection;

    private AnonymousBroadcaster mBroadcaster = new AnonymousBroadcaster();

    CentralSession(SessionInfo sessionInfo) {
        mSessionInfo = sessionInfo;
        mServiceLifecycleDelegate = new ServiceLifecycleDelegate();
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
     * コールバックを登録する
     */
    public void registerCallback(Object obj) {
        mBroadcaster.register(obj);
    }

    /**
     * コールバックを登録する
     */
    public void weakRegisterCallback(Object obj) {
        mBroadcaster.weakRegister(obj);
    }

    /**
     * コールバックを解除する
     */
    public void unregisterCallback(Object obj) {
        mBroadcaster.unregister(obj);
    }

    public long getSessionId() {
        return mSessionInfo.getSessionId();
    }

    public Clock getSessionClock() {
        return mSessionInfo.getSessionClock();
    }

    /**
     * 初期化を開始させる
     */
    public void initialize(@Nullable InitializeOption option) {
        // 現在の設定をDumpする
        async((BackgroundTask<ResultCollection> task) -> {
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

            mBroadcaster.safeEach(Listener.class, listener -> {
                listener.onInitializeCompleted(this);
            });
        }).start();
    }

    /**
     * 毎フレーム更新を行う
     */
    public void onUpdate(double deltaSec) {
        Timer timer = new Timer();
        try {
            // 内部時間を経過させる
            Clock sessionClock = mSessionInfo.getSessionClock();
            sessionClock.offset((long) (deltaSec * 1000));

            // 更新を行う
            mCentralDataManager.onUpdate();
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
     */
    @UiThread
    public void dispose() {
        AndroidThreadUtil.assertUIThread();
        async(ExecuteTarget.LocalQueue, CallbackTime.FireAndForget, task -> {
            if (mPluginCollection != null) {
                mPluginCollection.disconnect();
            }
            return this;
        }).finalized(task -> {
            mPluginCollection = null;
            mCentralDataManager = null;
        }).start();
        mServiceLifecycleDelegate.onDestroy();
    }

    public <T> BackgroundTaskBuilder<T> async(BackgroundTask.Async<T> background) {
        return mServiceLifecycleDelegate.asyncUI(background);
    }

    public <T> BackgroundTaskBuilder<T> async(ExecuteTarget execute, CallbackTime time, BackgroundTask.Async<T> background) {
        return mServiceLifecycleDelegate.async(execute, time, background);
    }

    public static CentralSession newInstance(@NonNull SessionInfo info) {
        CentralSession result = new CentralSession(info);

        Garnet.create(result)
                .depend(Context.class, info.getContext())
                .inject();

        return result;
    }

    public static class InitializeOption {
    }

    public interface Listener {
        @UiThread
        void onInitializeCompleted(CentralSession self);
    }
}
