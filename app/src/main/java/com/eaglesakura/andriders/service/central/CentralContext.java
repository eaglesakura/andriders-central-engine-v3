package com.eaglesakura.andriders.service.central;

import com.eaglesakura.andriders.central.CentralDataManager;
import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.central.command.CommandController;
import com.eaglesakura.andriders.central.command.ProximityCommandController;
import com.eaglesakura.andriders.central.command.SpeedCommandController;
import com.eaglesakura.andriders.db.AppSettings;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.db.command.CommandDataCollection;
import com.eaglesakura.andriders.db.command.CommandDatabase;
import com.eaglesakura.andriders.display.data.DataDisplayManager;
import com.eaglesakura.andriders.display.notification.NotificationDisplayManager;
import com.eaglesakura.andriders.display.notification.ProximityFeedbackManager;
import com.eaglesakura.andriders.plugin.CommandDataManager;
import com.eaglesakura.andriders.plugin.PluginConnector;
import com.eaglesakura.andriders.plugin.PluginManager;
import com.eaglesakura.andriders.provider.AppContextProvider;
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
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.ArrayList;
import java.util.List;

/**
 * ACEの表示状態
 */
public class CentralContext implements Disposable {

    @NonNull
    Service mContext;

    /**
     * システム時計
     */
    @NonNull
    final Clock mClock;

    /**
     * 更新用のタイマーリスト
     */
    @NonNull
    final MultiTimer mTimers;

    /**
     * 設定
     */
    @Inject(AppContextProvider.class)
    @NonNull
    AppSettings mSettings;

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
     * コマンド管理
     */
    @NonNull
    List<CommandController> mCommandControllers = new ArrayList<>();

    @NonNull
    ProximityFeedbackManager mProximityFeedbackManager;

    /**
     * 拡張機能管理
     */
    @NonNull
    PluginManager mPluginManager;

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
     * コマンド管理のインターバル
     */
    static final int COMMAND_UPDATE_INTERVAL_MS = 1000 / 15;

    /**
     * 通知レンダリングのフレームレート
     */
    static final int NOTIFICATION_UPDATE_INTERVAL_MS = 1000 / 30;

    /**
     * セントラルデータをDBに書き出すインターバル（秒）
     */
    static final int CENTRAL_COMMIT_INTERVAL_MS = 1000 * 30;

    private ServiceLifecycleDelegate mLifecycleDelegate = new ServiceLifecycleDelegate();

    /**
     * ACEのローカル情報を管理するレシーバ
     */
    CentralDataReceiver mLocalReceiver;

    public CentralContext(Service context, Clock updateClock) {
        mContext = context;
        mClock = updateClock;
        mTimers = new MultiTimer(mClock);
        mLocalReceiver = new CentralDataReceiver(context.getApplicationContext()) {
            @Override
            public void connect() {
                throw new Error("not call");
            }

            @Override
            public void disconnect() {
                throw new Error("not call");
            }
        };

        Garnet.inject(this);

        mCentralData = new CentralDataManager(context, mClock);
        mDisplayManager = new DataDisplayManager(mContext, mClock);
        mNotificationManager = new NotificationDisplayManager(mContext, mClock);
        mNotificationManager.addListener(new NotificationShowingListenerImpl(this));
        mProximityFeedbackManager = new ProximityFeedbackManager(mContext, mClock, getSubscription());

        mPluginManager = new PluginManager(mContext);
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

    @NonNull
    public NotificationDisplayManager getNotificationManager() {
        return mNotificationManager;
    }

    @NonNull
    public ProximityFeedbackManager getProximityFeedbackManager() {
        return mProximityFeedbackManager;
    }

    public PluginManager getPluginManager() {
        return mPluginManager;
    }

    public CentralDataReceiver getLocalReceiver() {
        return mLocalReceiver;
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
    private void initPlugins() throws Throwable {
        mPluginManager.connect(PluginManager.ConnectMode.ActiveOnly);
        for (PluginConnector client : mPluginManager.listClients()) {
            // サイコンデータ用コールバックを指定する
            client.setCentralWorker((PluginConnector.Action<CentralDataManager> action) -> {
                post(() -> action.callback(mCentralData));
            });

            // ディスプレイ設定用コールバックを指定する
            client.setDisplayWorker((PluginConnector.Action<DataDisplayManager> action) -> {
                post(() -> action.callback(mDisplayManager));
            });

            // 通知表示用コールバックを指定する
            client.setNotificationWorker(action -> {
                post(() -> action.callback(mNotificationManager));
            });
        }
    }

    /**
     * コマンド制御を初期化する
     */
    private void initCommands() throws Throwable {
        CommandDataManager commandDataManager = new CommandDataManager(mContext);

        // 近接コマンドセットアップ
        {
            ProximityCommandController proximityCommandController = new ProximityCommandController(mContext, mClock);
            proximityCommandController.setBootListener(new CommandBootListenerImpl(mContext, getSubscription()));
            mProximityFeedbackManager.bind(proximityCommandController);
            mCommandControllers.add(proximityCommandController);
        }
        // スピードコマンドを全て列挙し、コントローラを生成する
        {
            CommandDataCollection collection = commandDataManager.loadFromCategory(CommandDatabase.CATEGORY_SPEED);
            for (CommandData data : collection.list(it -> true)) {
                AppLog.system("Load SpeedCommand key[%s] package[[%s]", data.getKey().getKey(), data.getPackageName());
                SpeedCommandController controller = SpeedCommandController.newSpeedController(mContext, data);
                controller.bind(mLocalReceiver);
                mCommandControllers.add(controller);
            }
        }
    }

    /**
     * データ接続を開始する
     */
    public void onServiceInitializeCompleted() {
        mLifecycleDelegate.onCreate();
        newTask(SubscribeTarget.GlobalPipeline, task -> {
            initPlugins();
            task.throwIfCanceled();
            initCommands();
            task.throwIfCanceled();
            return this;
        }).completed((result, task) -> {
            mProximityFeedbackManager.connect();

            AppLog.system("Completed Initialize");
            mInitialized = true;

            // Pluginに起動完了を通知する
            for (PluginConnector plugin : mPluginManager.listClients()) {
                plugin.onCentralBootCompleted(this);
            }
        }).failed((error, task) -> {
            AppLog.system("Failed Initialize :: " + error.getMessage());
        }).start();
    }

    enum TimerType {
        CentralUpdate,
        CommandsUpdate,
        CentralBroadcast,
        CentralDbCommit,
        NotificationUpdate,
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

        // コマンドの更新を行う
        if (mTimers.endIfOverTime(TimerType.CommandsUpdate, COMMAND_UPDATE_INTERVAL_MS)) {
            for (CommandController controller : mCommandControllers) {
                controller.onUpdate();
            }
        }

        if (mTimers.endIfOverTime(TimerType.NotificationUpdate, NOTIFICATION_UPDATE_INTERVAL_MS)) {
            mNotificationManager.onUpdate();
        }

        // データのブロードキャストを行う
        if (mTimers.endIfOverTime(TimerType.CentralBroadcast, DATA_BROADCAST_INTERVAL_MS)) {
            broadcastCentralData();
        }

        // データのコミットを行う
        if (mTimers.endIfOverTime(TimerType.CentralDbCommit, CENTRAL_COMMIT_INTERVAL_MS)) {
            CentralContextImpl.commitLogDatabase(this);
        }
    }

    @Override
    public void dispose() {
        mProximityFeedbackManager.disconnect(); // 近接コマンドを切断する

        CentralContextImpl.commitLogDatabase(this);   // セントラルにコミットをかける

        // その他の終了タスクを投げる
        new RxTaskBuilder<>(getSubscription())
                .observeOn(ObserveTarget.FireAndForget)
                .subscribeOn(SubscribeTarget.GlobalPipeline)
                .async(task -> {
                    mPluginManager.disconnect();
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
     * データを各アプリへ送信する
     */
    @UiThread
    void broadcastCentralData() {
        RawCentralData data = mCentralData.getLatestCentralData();
        if (data == null) {
            AppLog.broadcast("mCentralData.getLatestCentralData() == null");
            return;
        }

        // ローカル伝達を行う
        mLocalReceiver.onReceived(data);

        Intent intent = new Intent();
        intent.setAction(CentralDataReceiver.ACTION_UPDATE_CENTRAL_DATA);
        intent.addCategory(CentralDataReceiver.INTENT_CATEGORY);
        try {
            intent.putExtra(CentralDataReceiver.EXTRA_CENTRAL_DATA, SerializeUtil.serializePublicFieldObject(data, true));
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            AppLog.report(e);
        }
    }
}
