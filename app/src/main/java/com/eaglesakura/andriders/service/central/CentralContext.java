package com.eaglesakura.andriders.service.central;

import com.eaglesakura.andriders.central.CentralDataManager;
import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.display.data.DataDisplayManager;
import com.eaglesakura.andriders.display.notification.NotificationDisplayManager;
import com.eaglesakura.andriders.plugin.PluginConnector;
import com.eaglesakura.andriders.plugin.PluginManager;
import com.eaglesakura.andriders.provider.StorageProvider;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.MultiTimer;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.io.Disposable;
import com.eaglesakura.util.SerializeUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * ACEの表示状態
 */
public class CentralContext implements Disposable {

    @NonNull
    private final Service mContext;

    /**
     * システム時計
     */
    @NonNull
    private final Clock mClock;

    /**
     * 更新用のタイマーリスト
     */
    @NonNull
    private final MultiTimer mTimers;

    /**
     * サイコン情報を表示する場合はtrue
     */
    private boolean mDisplayEnable = true;

    /**
     * 通知情報を表示する場合はtrue
     */
    private boolean mNotificationEnable = true;

    /**
     * 設定
     */
    @Inject(StorageProvider.class)
    @NonNull
    Settings mSettings;

    /**
     * サイコンデータ本体
     */
    @NonNull
    CentralDataManager mCentralData;

    /**
     * サイコン表示内容管理
     */
    @NonNull
    DataDisplayManager mDisplayManager;

    /**
     * 通知内容管理
     */
    @NonNull
    NotificationDisplayManager mNotificationManager;

    /**
     * 拡張機能管理
     */
    @NonNull
    PluginManager mExtensionClientManager;

    /**
     * 初期化が完了していればtrue
     */
    private boolean mInitialized = false;

    /**
     * データ送信を行うインターバル（ミリ秒）
     */
    static final int DATA_BROADCAST_INTERVAL_MS = 1000;

    /**
     * セントラルを更新するインターバル（ミリ秒）
     */
    static final int CENTRAL_UPDATE_INTERVAL_MS = 1000;

    /**
     * セントラルデータをDBに書き出すインターバル（秒）
     */
    static final int CENTRAL_COMMIT_INTERVAL_MS = 1000 * 30;

    private ServiceLifecycleDelegate mLifecycleDelegate = new ServiceLifecycleDelegate();

    public CentralContext(Service context, Clock updateClock) {
        mContext = context;
        mClock = updateClock;
        mTimers = new MultiTimer(mClock);

        Garnet.create(this)
                .depend(Context.class, context.getApplication())
                .inject();

        mCentralData = new CentralDataManager(context, mClock);
        mDisplayManager = new DataDisplayManager(mContext, mClock);
        mNotificationManager = new NotificationDisplayManager(mContext, mClock);

        mExtensionClientManager = new PluginManager(mContext);
    }

    /**
     * 現在時刻を取得する
     */
    public long now() {
        return mClock.now();
    }

    public DataDisplayManager getDisplayManager() {
        return mDisplayManager;
    }

    public NotificationDisplayManager getNotificationManager() {
        return mNotificationManager;
    }

    public PluginManager getExtensionClientManager() {
        return mExtensionClientManager;
    }

    /**
     * タスクを実行する。
     * Serviceが生きていれば実行されるが、Serviceが廃棄済みであればタスクは廃棄される。
     * タスクはUIスレッドで実行される。
     */
    public void post(Runnable task) {
        getSubscription().run(ObserveTarget.Alive, task);
    }

    /**
     * コールバック管理を取得する
     */
    public SubscriptionController getSubscription() {
        return mLifecycleDelegate.getSubscription();
    }

    /**
     * 拡張機能を初期化する
     */
    private void initExtensions() throws Throwable {
        mExtensionClientManager.connect(PluginManager.ConnectMode.Enabled);
        for (PluginConnector client : mExtensionClientManager.listClients()) {
            // サイコンデータ用コールバックを指定する
            client.setCentralWorker((PluginConnector.Action<CentralDataManager> action) -> {
                post(() -> {
                    action.callback(mCentralData);
                });
            });

            // TODO ディスプレイ設定用コールバックを指定する
            client.setDisplayWorker((PluginConnector.Action<DataDisplayManager> action) -> {
                post(() -> {
                    action.callback(mDisplayManager);
                });
            });
        }
    }

    /**
     * データ接続を開始する
     */
    public void onServiceInitializeCompleted() {
        mLifecycleDelegate.onCreate();
        newTask(SubscribeTarget.GlobalPipeline, task -> {
            initExtensions();
            return this;
        }).completed((result, task) -> {
            AppLog.system("Completed Initialize");
            mInitialized = true;
        }).failed((error, task) -> {
            AppLog.system("Failed Initialize :: " + error.getMessage());
        }).start();
    }

    enum TimerType {
        CentralUpdate,
        CentralBroadcast,
        CentralDbCommit,
    }

    /**
     * 定期更新を行う
     */
    public void onUpdated(final double deltaSec) {
        AndroidThreadUtil.assertUIThread();
        if (!mInitialized) {
            // 初期化が完了していないのでabort.
            return;
        }

        final long DIFF_TIME_MS = (long) (deltaSec * 1000.0);
        mClock.offset(DIFF_TIME_MS);

        // セントラルの更新を行う
        if (mTimers.endIfOverTime(TimerType.CentralUpdate, CENTRAL_UPDATE_INTERVAL_MS)) {
            mCentralData.onUpdate();
        }

        // データのブロードキャストを行う
        if (mTimers.endIfOverTime(TimerType.CentralBroadcast, DATA_BROADCAST_INTERVAL_MS)) {
            requestBroadcastBasicDatas();
        }

        // データのコミットを行う
        if (mTimers.endIfOverTime(TimerType.CentralDbCommit, CENTRAL_COMMIT_INTERVAL_MS)) {
            requestCommitDatabase();
        }
    }

    @Override
    public void dispose() {
        requestCommitDatabase();    // セントラルにコミットをかける

        // その他の終了タスクを投げる
        new RxTaskBuilder<>(getSubscription())
                .observeOn(ObserveTarget.FireAndForget)
                .subscribeOn(SubscribeTarget.GlobalPipeline)
                .async(task -> {
                    mExtensionClientManager.disconnect();
                    return this;
                })
                .finalized(task -> {
                    AppLog.system("Finished session");
                }).start();

        // タスクをシャットダウンする
        mLifecycleDelegate.onDestroy();
    }

    /**
     * 非同期タスクを生成する
     */
    public <T> RxTaskBuilder<T> newTask(SubscribeTarget subscribeTarget, RxTask.Async<T> task) {
        return new RxTaskBuilder<T>(getSubscription())
                .async(task)
                .observeOn(ObserveTarget.Alive)
                .subscribeOn(subscribeTarget);
    }

    /**
     * DBの内容を書き出す
     */
    void requestCommitDatabase() {
        new RxTaskBuilder<>(getSubscription())
                .observeOn(ObserveTarget.FireAndForget)
                .subscribeOn(SubscribeTarget.GlobalPipeline)
                .async(task -> {
                    AppLog.db("CentralCommit Start");
                    mCentralData.commit();
                    return this;
                })
                .completed((result, task) -> {
                    AppLog.db("CentralCommit Completed");
                })
                .start();
    }


    /**
     * データを各アプリへ送信する
     */
    void requestBroadcastBasicDatas() {
        RawCentralData data = mCentralData.getLatestCentralData();
        if (data == null) {
            AppLog.broadcast("mCentralData.getLatestCentralData() == null");
        }
        Intent intent = new Intent();
        intent.setAction(CentralDataReceiver.INTENT_ACTION);
        intent.addCategory(CentralDataReceiver.INTENT_CATEGORY);
        try {
            intent.putExtra(CentralDataReceiver.INTENT_EXTRA_CENTRAL_DATA, SerializeUtil.serializePublicFieldObject(data, true));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mContext.sendBroadcast(intent);
    }
}
