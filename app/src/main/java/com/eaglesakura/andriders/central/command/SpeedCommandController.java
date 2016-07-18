package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.central.SensorDataReceiver;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.android.rx.SubscriptionController;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * スピードコマンドの実行管理を行う
 */
public abstract class SpeedCommandController extends CommandController {

    /**
     * 今日の最高速度
     */
    double mTodayMaxSpeedKmh = -1;

    /**
     * 最高速度
     */
    double mMaxSpeedKmh = -1;

    /**
     * 前回更新時の速度
     */
    double mBeforeSpeedKmh;

    /**
     * 現在の速度
     */
    double mCurrentSpeedKmh;

    public SpeedCommandController(@NonNull Context context, @NonNull SubscriptionController subscriptionController) {
        super(context, subscriptionController);
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
            // 今日の最高速度更新
            if (mTodayMaxSpeedKmh < 0) {
                mTodayMaxSpeedKmh = master.record.maxSpeedKmhToday;
            }

            // いままでの最高速度
            if (mMaxSpeedKmh < 0) {
                mMaxSpeedKmh = master.record.maxSpeedKmh;
            }
        }

        @Override
        public void onUpdated(@NonNull RawCentralData master, @Nullable RawSensorData.RawSpeed oldValue, @NonNull RawSensorData.RawSpeed newValue) {

        }
    };

}
