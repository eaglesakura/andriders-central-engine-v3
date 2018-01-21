package com.eaglesakura.andriders.central.service;

import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.gen.prop.DebugSettings;
import com.eaglesakura.andriders.gen.prop.UserProfiles;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.CentralPluginCollection;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.cerberus.error.TaskCanceledException;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.Timer;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 1セッションを管理する。
 * これはCommandService経由でリモートからも扱えるようにする。
 */
public class CentralSession {

    @NonNull
    final SessionInfo mSessionInfo;

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
    SessionState.Bus mStateBus = new SessionState.Bus(new SessionState(SessionState.State.NewObject, this));

    /**
     * データ取得用のStream
     */
    @NonNull
    private SessionDataStream mSessionDataStream;

    CentralSession(SessionInfo sessionInfo) {
        mSessionInfo = sessionInfo;
        mSessionDataStream = new SessionDataStream(this);
    }

    /**
     * セッションの基本状態を取得する
     */
    @NonNull
    public SessionInfo getSessionInfo() {
        return mSessionInfo;
    }

    /**
     * State通知用のBusを取得する
     */
    @NonNull
    public SessionState.Bus getStateBus() {
        return mStateBus;
    }

    /**
     * セッション情報を受け取るStream
     */
    @NonNull
    public SessionDataStream getDataStream() {
        return mSessionDataStream;
    }

    public long getSessionId() {
        return mSessionInfo.getSessionId();
    }

    public Clock getSessionClock() {
        return mSessionInfo.getSessionClock();
    }

    @NonNull
    public CentralPluginCollection getPluginCollection() {
        return mPluginCollection;
    }

    /**
     * 初期化を開始させる
     */
    public void initialize(@Nullable InitializeOption option, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        mStateBus.modified(new SessionState(SessionState.State.Initializing, this));

        CentralPluginCollection pluginCollection;

        // 既存のログを読み込む
        LogStatistics allStatistics = mCentralLogManager.loadAllStatistics(cancelCallback);
        LogStatistics todayStatistics = mCentralLogManager.loadDailyStatistics(getSessionClock().now(), cancelCallback);

        // Centralモードで接続する
        {
            pluginCollection = mPluginDataManager.listPlugins(PluginDataManager.PluginListingMode.Active, cancelCallback);
            CentralPlugin.ConnectOption connectOption = new CentralPlugin.ConnectOption();
            connectOption.centralConnection = true;
            pluginCollection.connect(connectOption, cancelCallback);
        }

        mCentralDataManager = new CentralDataManager(mSessionInfo, allStatistics, todayStatistics);
        mPluginCollection = pluginCollection;

        // State切り替えを通知する
        mStateBus.modified(new SessionState(SessionState.State.Running, this));

        // 必要であればWi-Fiを切断する
        if (mSessionInfo.getCentralServiceSettings().isWifiDisable()) {
            try {
                WifiManager wifiManager = (WifiManager) mSessionInfo.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    wifiManager.setWifiEnabled(false);
                }
            } catch (Exception e) {
                AppLog.report(e);
            }
        }
    }

    /**
     * 毎フレーム更新を行う
     */
    public void onUpdate(double deltaSec) {
        // 実行中以外のステートは無視する
        if (mStateBus.getState() != SessionState.State.Running) {
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
                mSessionDataStream.onUpdate(mCentralDataManager.getLatestCentralData());
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
    public void dispose() throws AppException {
        try {
            mStateBus.modified(new SessionState(SessionState.State.Stopping, this));
            if (mPluginCollection != null) {
                mPluginCollection.disconnect();
            }
        } finally {
            mStateBus.modified(new SessionState(SessionState.State.Destroyed, this));
        }
    }

    public static CentralSession newInstance(@NonNull SessionInfo info) {
        CentralSession result = new CentralSession(info);

        Garnet.create(result)
                .depend(Context.class, info.getContext())
                .depend(UserProfiles.class, info.getUserProfiles())
                .depend(DebugSettings.class, info.getDebugSettings())
                .inject();

        return result;
    }

    public static class InitializeOption {
    }
}
