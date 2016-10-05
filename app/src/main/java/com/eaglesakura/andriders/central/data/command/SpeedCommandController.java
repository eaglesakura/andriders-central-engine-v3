package com.eaglesakura.andriders.central.data.command;

import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.central.SensorDataReceiver;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawRecord;
import com.eaglesakura.andriders.serialize.RawSensorData;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * スピードコマンドの実行管理を行う
 */
public abstract class SpeedCommandController extends CommandController {

    @Nullable
    RawRecord mRecord;

    /**
     * 前回更新時の速度
     */
    double mBeforeSpeedKmh;

    /**
     * 現在の速度
     */
    double mCurrentSpeedKmh;

    /**
     * 基準速度
     */
    final double mSpeedKmh;

    /**
     * コマンドタイプ
     */
    final int mCommandType;

    final CommandData mCommandData;

    public SpeedCommandController(@NonNull Context context, CommandData commandData) {
        super(context);
        mCommandData = commandData;

        CommandData.Extra extra = mCommandData.getInternalExtra();
        mSpeedKmh = extra.speedKmh;
        mCommandType = extra.speedType;
    }

    /**
     * ハンドラをバインドする
     */
    public void bind(CentralDataReceiver receiver) {
        receiver.addHandler(mSpeedHandler);
    }

    /**
     * 速度更新処理
     */
    void onUpdateSpeed(float currentSpeedKmh) {
        mBeforeSpeedKmh = mCurrentSpeedKmh;
        mCurrentSpeedKmh = currentSpeedKmh;
    }

    /**
     * 指定速度を超えた瞬間であればtrueを返却する
     */
    protected boolean nowSpeedUpper(double kmh) {
        if (mBeforeSpeedKmh == 0 && mCurrentSpeedKmh == 0) {
            // 接続が確立されていない
            return false;
        }

        return mBeforeSpeedKmh < kmh // 以前のチェックでは規定速度を超えていなかった
                && mCurrentSpeedKmh >= kmh; // 現在は規定速度を超えている
    }

    /**
     * 指定速度を下回った瞬間であればtrueを返却する
     */
    protected boolean nowSpeedLower(double kmh) {
        if (mBeforeSpeedKmh == 0 && mCurrentSpeedKmh == 0) {
            // 接続が確立されていない
            return false;
        }

        return mBeforeSpeedKmh > kmh // 以前のチェックでは規定速度を超えていた
                && mCurrentSpeedKmh <= kmh; // 現在は規定速度を下回っている
    }

    final SensorDataReceiver.SpeedHandler mSpeedHandler = new SensorDataReceiver.SpeedHandler() {
        @Override
        public void onReceived(@NonNull RawCentralData master, @NonNull RawSensorData.RawSpeed sensor) {
            // 最高速度の更新
            mRecord = master.record;
            onUpdateSpeed(sensor.speedKmPerHour);
        }

        @Override
        public void onUpdated(@NonNull RawCentralData master, @Nullable RawSensorData.RawSpeed oldValue, @NonNull RawSensorData.RawSpeed newValue) {

        }
    };

    /**
     * スピードコマンド用コントローラを生成する
     */
    public static SpeedCommandController newSpeedController(Context context, CommandData data) {
        final int type = data.getInternalExtra().speedType;
        SpeedCommandController controller;
        switch (type) {
            case CommandData.SPEED_TYPE_UPPER:
            case CommandData.SPEED_TYPE_LOWER:
                controller = new BasicSpeedCommandController(context, data);
                break;
            default:
                controller = new MaxSpeedCommandController(context, data);
                break;
        }
        return controller;
    }
}
