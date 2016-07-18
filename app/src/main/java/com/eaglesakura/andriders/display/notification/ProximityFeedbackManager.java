package com.eaglesakura.andriders.display.notification;

import com.eaglesakura.andriders.central.command.ProximityCommandController;
import com.eaglesakura.andriders.db.AppSettings;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.provider.StorageProvider;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.android.device.vibrate.VibrateManager;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.android.system.ScreenEventReceiver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.util.List;

/**
 * 近接コマンドの操作を行う
 */
public class ProximityFeedbackManager {
    @NonNull
    Context mContext;

    @NonNull
    final ClockTimer mTimer;

    @Inject(StorageProvider.class)
    AppSettings mAppSettings;

    /**
     * 近接状態である場合true
     */
    boolean mProximityState;

    /**
     * 最後の近接状態チェック
     */
    boolean mLastProximityState = false;

    /**
     * 近接センサーとして許可する値
     */
    private static final float COMMAND_INPUT_VALUE = 0.1f;

    @NonNull
    final SensorManager mSensorManager;

    @Nullable
    CommandData mCommandData;

    ScreenEventReceiver mScreenEventReceiver;

    ProximityCommandController mCommandController;

    final SubscriptionController mSubscriptionController;

    int mTaskId = 0;

    public ProximityFeedbackManager(@NonNull Context context, @NonNull Clock clock, @NonNull SubscriptionController subscriptionController) {
        mContext = context;
        mTimer = new ClockTimer(clock);
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mScreenEventReceiver = new ScreenEventReceiver(mContext);
        mSubscriptionController = subscriptionController;

        Garnet.create(this)
                .depend(Context.class, context)
                .inject();
    }

    /**
     * コントローラに
     */
    public void bind(@NonNull ProximityCommandController commandController) {
        commandController.setProximityListener(mProximityFeedbackListener);
        mCommandController = commandController;
    }


    /**
     * 近接センサーに接続する
     */
    @UiThread
    public void connect() {
        mScreenEventReceiver.connect();

        // 近接コマンドに接続する
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_PROXIMITY);
        if (!sensorList.isEmpty()) {
            mSensorManager.registerListener(mProximityListener, sensorList.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    /**
     * 近接センサーから切断する
     */
    @UiThread
    public void disconnect() {
        mScreenEventReceiver.disconnect();

        try {
            mSensorManager.unregisterListener(mProximityListener);
        } catch (Exception e) {
            AppLog.report(e);
        }
    }

    private boolean isScreenLink() {
        return mAppSettings.getCentralSettings().getProximityCommandScreenLink();
    }


    /**
     * センサーイベントの受理を行う
     */
    private SensorEventListener mProximityListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(final SensorEvent event) {
            float value = event.values[0];

            if (isScreenLink() && !mScreenEventReceiver.isScreenPowerOn()) {
                // スクリーンリンクされていて、スクリーンOFFならコマンドに反応しない
                value = 1000;
            }

            AppLog.proximity("Proximity[%.1f]", value);

            mLastProximityState = mProximityState;
            mProximityState = (value < COMMAND_INPUT_VALUE);
            if (mLastProximityState == mProximityState) {
                // ステートが変わってない
                return;
            }

            // ステートが変わった
            ++mTaskId;
            if (mProximityState) {
                // 近接状態の監視を行う
                startProximityCheckTask(mTaskId);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    /**
     * 近接状態の監視を行う
     */
    @UiThread
    void startProximityCheckTask(int taskId) {
        new RxTaskBuilder<>(mSubscriptionController)
                .observeOn(ObserveTarget.Alive)
                .subscribeOn(SubscribeTarget.NewThread)
                .async(task -> {
                    try {
                        mCommandController.onStartCount();
                        while (!task.isCanceled()) {
                            task.waitTime(100);
                        }
                    } finally {
                        if (!mProximityState) {
                            mCommandController.onEndCount();
                        }
                    }
                    return this;
                })
                .cancelSignal(task -> {
                    return taskId == mTaskId && mProximityState;
                })
                .start();
    }

    ProximityCommandController.ProximityListener mProximityFeedbackListener = (self, sec, data) -> {
        if (!mProximityState) {
            // 近接してないので何もしない
            mCommandData = null;
            return;
        }

        mCommandData = data;
        if (data != null) {
            AppLog.proximity("BootCommand[%s]", data.getPackageName());
        }

        if (sec == 0) {
            // 開始フィードバック
            VibrateManager.vibrate(mContext, VibrateManager.VIBRATE_TIME_SHORT_MS * 2);
        } else {
            // 中途フィードバック
            VibrateManager.vibrateLong(mContext);
        }
    };
}
