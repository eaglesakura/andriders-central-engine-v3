package com.eaglesakura.andriders.service.command;

import com.eaglesakura.andriders.AceSdkUtil;
import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.central.data.command.CommandController;
import com.eaglesakura.andriders.central.data.command.distance.DistanceCommandController;
import com.eaglesakura.andriders.central.data.command.proximity.ProximityCommandController;
import com.eaglesakura.andriders.central.data.command.speed.SpeedCommandController;
import com.eaglesakura.andriders.central.data.command.timer.TimerCommandController;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionState;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.command.CommandSetting;
import com.eaglesakura.andriders.command.SerializableIntent;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.command.CommandDataCollection;
import com.eaglesakura.andriders.plugin.CommandDataManager;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.provider.SessionManagerProvider;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawIntent;
import com.eaglesakura.andriders.service.ui.AnimationFrame;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.sloth.app.lifecycle.Lifecycle;
import com.eaglesakura.sloth.app.lifecycle.ServiceLifecycle;
import com.squareup.otto.Subscribe;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.util.ArrayList;
import java.util.List;

/**
 * コマンド管理を行う
 */
public class CentralCommandController {

    @NonNull
    private Context mContext;

    @NonNull
    private Callback mCallback;

    /**
     * 各パラメータのハンドリングを容易にするため、振り分けのためのレシーバを持つ
     */
    @NonNull
    final private CentralDataReceiver mCentralDataReceiver;

    @NonNull
    final private CentralSession mSession;

    @NonNull
    final private Lifecycle mLifecycleDelegate;

    @Inject(AppManagerProvider.class)
    private CommandDataManager mCommandDataManager;

    /**
     * 近接センサー管理
     */
    private ProximitySensorManager mProximitySensorManager;

    /**
     * 近接センサーフィードバック
     */
    private ProximityFeedbackManager mProximityFeedbackManager;

    @Nullable
    private ProximityCommandController mProximityCommandController;

    final List<DistanceCommandController> mDistanceCommandControllerList = new ArrayList<>();

    final List<SpeedCommandController> mSpeedCommandControllerList = new ArrayList<>();

    final List<TimerCommandController> mTimerCommandControllerList = new ArrayList<>();

    @NonNull
    private AnimationFrame.Bus mAnimationBus;

    @Nullable
    private RawCentralData mLatestCentralData;

    CentralCommandController(@NonNull Context context, Lifecycle delegate, CentralSession session, AnimationFrame.Bus animationFrameBus, Callback callback) {
        mContext = context;
        mCallback = callback;
        mCentralDataReceiver = new CentralDataReceiver(mContext);
        mLifecycleDelegate = delegate;
        mSession = session;
        mAnimationBus = animationFrameBus;
    }

    public static CentralCommandController attach(@NonNull Context context, ServiceLifecycle lifecycleDelegate, AnimationFrame.Bus animationFrameBus, @NonNull CentralSession session, Callback callback) {
        CentralCommandController result = new CentralCommandController(context, lifecycleDelegate, session, animationFrameBus, callback);
        Garnet.create(result)
                .depend(Context.class, context)
                .inject();
        session.getDataStream().observe(lifecycleDelegate, result::observeCentralData);
        session.getStateBus().bind(lifecycleDelegate, result);
        animationFrameBus.bind(lifecycleDelegate, result);
        return result;
    }

    public ProximityFeedbackManager getProximityFeedbackManager() {
        return mProximityFeedbackManager;
    }

    /**
     * Sessionのステート変更通知をハンドリングする
     */
    @Subscribe
    private void onSessionStateChanged(SessionState.Bus state) {
        AppLog.system("SessionState ID[%d] Changed[%s]", state.getSession().getSessionId(), state.getState());

        if (state.getState() == SessionState.State.Running) {
            // すべての設定済みコマンドをロードする
            // 必要であれば近接コマンドへ接続する
            loadCommands();
        } else if (state.getState() == SessionState.State.Stopping) {
            // 必要であれば近接コマンドから切断する
            if (mProximitySensorManager != null) {
                mProximitySensorManager.disconnect();
                mProximitySensorManager = null;
            }
        }
    }

    @UiThread
    private void loadCommands() {
        mLifecycleDelegate.async(ExecuteTarget.LocalQueue, CallbackTime.Alive, (BackgroundTask<CommandDataCollection> task) -> {
            return mCommandDataManager.loadAll();
        }).completed((result, task) -> {
            AppLog.command("Loaded Commands[%d]", result.size());
            for (CommandData data : result.getSource()) {
                AppLog.command("  - key[%s]", data.getKey());
            }
            onLoadCommands(result);
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
    }

    /**
     * DBからロードしたコマンドデータをバインドする
     * 近接コマンドは特殊処理が行われるため、それ以外のみで良い。
     */
    private void onLoadCommands(CommandDataCollection allCommands) {

        List<CommandData> proximityCommands = allCommands.list(cmd -> cmd.getCategory() == CommandData.CATEGORY_PROXIMITY);
        // 近接コマンドはセンサーと連動するので別扱い
        if (!proximityCommands.isEmpty()) {
            mProximityCommandController = new ProximityCommandController(mContext);
            mProximityCommandController.setBootListener(mCommandBootListener);
            mProximityCommandController.setCommands(new CommandDataCollection(proximityCommands));

            // 近接センサーをセットアップ
            mProximitySensorManager = Garnet.factory(SessionManagerProvider.class).depend(CentralSession.class, mSession).instance(ProximitySensorManager.class);
            // データを接続
            mProximitySensorManager.connect();

            // フィードバックを設定
            mProximityFeedbackManager = Garnet.factory(SessionManagerProvider.class)
                    .depend(CentralSession.class, mSession)
                    .instance(ProximityFeedbackManager.class);
            mProximityFeedbackManager.setProximityCommands(new CommandDataCollection(proximityCommands));

            // Busに登録する
            mProximitySensorManager.getProximityDataBus().bind(mLifecycleDelegate, this);
            mProximitySensorManager.getProximityDataBus().bind(mLifecycleDelegate, mProximityFeedbackManager);
            mAnimationBus.bind(mLifecycleDelegate, mProximityFeedbackManager);
        }

        // その他のコマンドを付与する
        for (CommandData data : allCommands.getSource()) {
            AppLog.command("Command key[%s]", data.getKey());
            switch (data.getCategory()) {
                case CommandData.CATEGORY_DISTANCE: {
                    AppLog.command(" -> DistanceCommand");
                    DistanceCommandController controller = new DistanceCommandController(mContext, data);
                    controller.bind(mCentralDataReceiver);
                    controller.setBootListener(mCommandBootListener);
                    mDistanceCommandControllerList.add(controller);
                }
                break;
                case CommandData.CATEGORY_SPEED: {
                    AppLog.command(" -> SpeedCommand");
                    SpeedCommandController controller = SpeedCommandController.newSpeedController(mContext, data);
                    controller.bind(mCentralDataReceiver);
                    controller.setBootListener(mCommandBootListener);
                    mSpeedCommandControllerList.add(controller);
                }
                break;
                case CommandData.CATEGORY_TIMER: {
                    AppLog.command(" -> TimerCommand");
                    TimerCommandController controller =
                            new TimerCommandController(mContext, data, mSession.getSessionInfo().getSessionClock().now());
                    controller.setBootListener(mCommandBootListener);
                    mTimerCommandControllerList.add(controller);
                }
                break;
            }
        }

        mCallback.onCommandLoaded(this, allCommands);
    }

    /**
     * 近接コマンド状態が更新された
     */
    @Subscribe
    private void onUpdateProximity(ProximityData.Bus data) {
        mProximityCommandController.onUpdate(data.getData());
    }

    /**
     * データ更新をハンドリングする
     */
    @UiThread
    private void observeCentralData(RawCentralData data) {
        mLatestCentralData = data;
        mCentralDataReceiver.onReceived(mLatestCentralData);
    }

    /**
     * 毎フレーム処理の振り分けを行う
     */
    @Subscribe
    private void onAnimationFrame(AnimationFrame.Bus frame) {
//        AppLog.system("onAnimationFrame frame[%d] delta[%.3f sec]", frame.getFrameCount(), frame.getDeltaSec());

        // 必要なコントローラを更新する
        for (TimerCommandController controller : mTimerCommandControllerList) {
            controller.onUpdate(mSession.getSessionClock().now());
        }
    }

    private final CommandController.CommandBootListener mCommandBootListener = ((self, data) -> {
        byte[] centralData = null;
        try {
            AppLog.command("Boot Command[%s]", data.getKey());
            RawIntent rawIntent = data.getIntent();
            Intent intent = SerializableIntent.newIntent(rawIntent);
            // ACEのデータを受け取っているなら、シリアライズしてコマンドに送る
            if (mLatestCentralData != null) {
                centralData = AceSdkUtil.serializeToByteArray(mLatestCentralData);
                intent.putExtra(CommandSetting.EXTRA_COMMAND_CENTRAL_DATA, centralData);
            }
            switch (rawIntent.intentType) {
                case Activity:
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 新規Taskでなければならない
                    mCallback.requestActivityCommand(this, data, intent);
                    break;
                case Service:
                    mCallback.requestServiceCommand(this, data, intent);
                    break;
                case Broadcast:
                    intent.setPackage(data.getPackageName());   // 対象packageを固定する
                    mCallback.requestBroadcastCommand(this, data, intent);
                    break;
            }

        } catch (Exception e) {
            AppLog.printStackTrace(e);
            return;
        }

        // 正常に起動できたら、Receiverにも流す
        try {
            Intent intent = new Intent(CentralDataReceiver.ACTION_COMMAND_BOOTED);
            CommandKey.putExtra(intent, CentralDataReceiver.EXTRA_COMMAND_KEY, data.getKey());
            if (centralData != null) {
                intent.putExtra(CentralDataReceiver.EXTRA_CENTRAL_DATA, centralData);
            }
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            AppLog.printStackTrace(e);
        }
    });

    public interface Callback {

        /**
         * コマンド情報のロードが完了した
         *
         * @param commands ロードしたコマンド一覧
         */
        void onCommandLoaded(CentralCommandController self, CommandDataCollection commands);

        void requestActivityCommand(CentralCommandController self, CommandData data, Intent commandIntent);

        void requestBroadcastCommand(CentralCommandController self, CommandData data, Intent commandIntent);

        void requestServiceCommand(CentralCommandController self, CommandData data, Intent commandIntent);
    }
}
