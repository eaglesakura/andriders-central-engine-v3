package com.eaglesakura.andriders.service.central;

import com.eaglesakura.andriders.central.CentralDataManager;
import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.display.data.DataDisplayManager;
import com.eaglesakura.andriders.display.notification.NotificationDisplayManager;
import com.eaglesakura.andriders.extension.ExtensionClient;
import com.eaglesakura.andriders.extension.ExtensionClientManager;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.andriders.util.MultiTimer;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.io.Disposable;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.SerializeUtil;

import android.app.Service;
import android.content.Intent;
import android.support.annotation.NonNull;

import rx.subjects.BehaviorSubject;

/**
 * ACEの表示状態
 */
public class CentralContext implements Disposable {

    /**
     * 設定
     */
    private final Settings mSettings = Settings.getInstance();

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
     * サイコンデータ本体
     */
    @NonNull
    private final CentralDataManager mCentralData;

    /**
     * サイコン表示内容管理
     */
    @NonNull
    private final DataDisplayManager mDisplayManager;

    /**
     * 通知内容管理
     */
    @NonNull
    private final NotificationDisplayManager mNotificationManager;

    /**
     * 拡張機能管理
     */
    @NonNull
    private final ExtensionClientManager mExtensionClientManager;

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

    private BehaviorSubject<LifecycleState> mSubject = BehaviorSubject.create(LifecycleState.NewObject);

    private SubscriptionController mSubscriptionController = new SubscriptionController().bind(mSubject);

    public CentralContext(Service context, Clock updateClock) {
        mContext = context;
        mClock = updateClock;
        mTimers = new MultiTimer(mClock);

        mCentralData = new CentralDataManager(context, mClock);
        mDisplayManager = new DataDisplayManager(mContext, mClock);
        mNotificationManager = new NotificationDisplayManager(mContext, mClock);

        mExtensionClientManager = new ExtensionClientManager(mContext);
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

    public ExtensionClientManager getExtensionClientManager() {
        return mExtensionClientManager;
    }

    /**
     * タスクを実行する。
     * Serviceが生きていれば実行されるが、Serviceが廃棄済みであればタスクは廃棄される。
     * タスクはUIスレッドで実行される。
     */
    public void post(Runnable task) {
        mSubscriptionController.run(ObserveTarget.Alive, task);
    }

    /**
     * コールバック管理を取得する
     */
    public SubscriptionController getSubscriptionController() {
        return mSubscriptionController;
    }

    /**
     * 拡張機能を初期化する
     */
    private void initExtensions() throws Throwable {
        mExtensionClientManager.connect(ExtensionClientManager.ConnectMode.Enabled);
        for (ExtensionClient client : mExtensionClientManager.listClients()) {
            // サイコンデータ用コールバックを指定する
            client.setCentralWorker((ExtensionClient.Action<CentralDataManager> action) -> {
                post(() -> {
                    action.callback(mCentralData);
                });
            });

            // TODO ディスプレイ設定用コールバックを指定する
            client.setDisplayWorker((ExtensionClient.Action<DataDisplayManager> action) -> {
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
        mSubject.onNext(LifecycleState.OnCreated);
        mSubject.onNext(LifecycleState.OnStarted);
        mSubject.onNext(LifecycleState.OnResumed);
        newTask(SubscribeTarget.GlobalPipeline, task -> {
            initExtensions();
            return this;
        }).completed((result, task) -> {
            LogUtil.log("Completed Initialize");
            mInitialized = true;
        }).failed((error, task) -> {
            LogUtil.log("Failed Initialize :: " + error.getMessage());
        }).start();
    }

    enum TimerType {
        CentralUpdate,
        CentralBroadcast,
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

        if (mTimers.endIfOverTime(TimerType.CentralBroadcast, DATA_BROADCAST_INTERVAL_MS)) {
            requestBroadcastBasicDatas();
        }
    }

    @Override
    public void dispose() {
        // TODO セッション終了タスクを発行する
        newTask(SubscribeTarget.GlobalPipeline, task -> {
            mExtensionClientManager.disconnect();
            return this;
        })
                .observeOn(ObserveTarget.FireAndForget) // コールバックは常に行われる
                .finalized(task -> {
                    LogUtil.log("Finished session");
                })
                .start();

        mSubject.onNext(LifecycleState.OnPaused);
        mSubject.onNext(LifecycleState.OnStopped);
        mSubject.onNext(LifecycleState.OnDestroyed);
    }

    /**
     * 非同期タスクを生成する
     */
    public <T> RxTaskBuilder<T> newTask(SubscribeTarget subscribeTarget, RxTask.Async<T> task) {
        return new RxTaskBuilder<T>(getSubscriptionController())
                .async(task)
                .observeOn(ObserveTarget.Alive)
                .subscribeOn(subscribeTarget);
    }

    /**
     * TODO: データを定期送信する
     * TODO: 拡張Serviceにデータを送信する
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
