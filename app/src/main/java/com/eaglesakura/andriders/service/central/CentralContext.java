package com.eaglesakura.andriders.service.central;

import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.central.CycleComputerData;
import com.eaglesakura.andriders.computer.display.DisplayManager;
import com.eaglesakura.andriders.computer.extension.client.ExtensionClient;
import com.eaglesakura.andriders.computer.extension.client.ExtensionClientManager;
import com.eaglesakura.andriders.computer.notification.NotificationManager;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.io.Disposable;
import com.eaglesakura.util.LogUtil;

import android.app.Service;
import android.content.Intent;

import rx.subjects.BehaviorSubject;

/**
 * ACEの表示状態
 */
public class CentralContext implements Disposable {

    /**
     * 設定
     */
    private final Settings mSettings = Settings.getInstance();

    private final Service mContext;

    /**
     * システム時計
     */
    private final Clock mClock;

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
    private CycleComputerData mCycleComputerData;

    /**
     * サイコン表示内容管理
     */
    private DisplayManager mDisplayManager;

    /**
     * 通知内容管理
     */
    private NotificationManager mNotificationManager;

    /**
     * 拡張機能管理
     */
    private ExtensionClientManager mExtensionClientManager;

    /**
     * 初期化が完了していればtrue
     */
    private boolean mInitialized = false;

    /**
     * データ送信を行うインターバル（ミリ秒）
     */
    static final int DATA_BROADCAST_INTERVAL_MS = 1000;

    /**
     * 最終ブロードキャスト時刻
     */
    private long mLastBroadcastTime;

    private BehaviorSubject<LifecycleState> mSubject = BehaviorSubject.create(LifecycleState.NewObject);

    private SubscriptionController mSubscriptionController = new SubscriptionController().bind(mSubject);

    public CentralContext(Service context, Clock updateClock) {
        mContext = context;
        mClock = updateClock;

        mCycleComputerData = new CycleComputerData(context, mClock.now());
        mDisplayManager = new DisplayManager(mContext, mSubscriptionController);
        mNotificationManager = new NotificationManager(mContext, mSubscriptionController);

        mExtensionClientManager = new ExtensionClientManager(mContext);
    }

    /**
     * 現在時刻を取得する
     */
    public long now() {
        return mClock.now();
    }

    public DisplayManager getDisplayManager() {
        return mDisplayManager;
    }

    public NotificationManager getNotificationManager() {
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
            client.setWorker((ExtensionClient.Action<CycleComputerData> action) -> {
                post(() -> {
                    action.callback(mCycleComputerData);
                });
            });

            // TODO ディスプレイ設定用コールバックを指定する
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
            mLastBroadcastTime = now();
        }).failed((error, task) -> {
            LogUtil.log("Failed Initialize :: " + error.getMessage());
        }).start();
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
        // TODO Centralの時間を進める
        // TODO 通知の更新を行う

        if ((now() - mLastBroadcastTime) > DATA_BROADCAST_INTERVAL_MS) {
            // インターバルを超えたので、データのブロードキャストを行う
            mLastBroadcastTime = System.currentTimeMillis();
            requestBroadcastBasicDatas();
        }
    }

    @Override
    public void dispose() {
        // TODO セッション終了タスクを発行する
        newTask(SubscribeTarget.GlobalPipeline, task -> {
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
        Intent intent = new Intent();
    }
}
