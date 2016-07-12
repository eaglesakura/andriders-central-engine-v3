package com.eaglesakura.andriders.central.scsensor;

import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.andriders.db.AppSettings;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.central.base.BaseCalculator;

import android.support.annotation.NonNull;

public class SensorSpeedData extends BaseCalculator {

    /**
     * ホイールの回転数
     */
    private float mWheelRpm;

    /**
     * ホイールの合計回転数
     */
    private int mWheelRevolution;

    /**
     * 前回更新時刻
     */
    private long mUpdatedTime;

    /**
     * 計算済みの速度
     */
    private double mSpeedKmh;

    /**
     * 最高速度
     */
    private double mMaxSpeedKmh;

    @NonNull
    AppSettings mSettings;

    public SensorSpeedData(Clock clock, @NonNull AppSettings settings) {
        super(clock);
        mSettings = settings;
    }

    public boolean valid() {
        return (now() - mUpdatedTime) < DATA_TIMEOUT_MS;
    }

    public long getUpdatedTime() {
        return mUpdatedTime;
    }

    /**
     * 現在の速度を取得する
     */
    public double getSpeedKmh() {
        if (valid()) {
            return mSpeedKmh;
        } else {
            return 0;
        }
    }

    /**
     * 最高速度を取得する
     */
    public double getMaxSpeedKmh() {
        return mMaxSpeedKmh;
    }

    public float getWheelRpm() {
        return mWheelRpm;
    }

    public int getWheelRevolution() {
        return mWheelRevolution;
    }

    /**
     * ホイールの外周サイズ（mm）を取得する
     */
    public float getWheelOuterLength() {
        return mSettings.getUserProfiles().getWheelOuterLength();
    }

    /**
     * センサー由来の速度を更新する
     *
     * @param wheelRpm        ホイール回転数
     * @param wheelRevolution ホイール回転数合計
     * @return 更新したらtrue
     */
    public boolean setSensorSpeed(float wheelRpm, int wheelRevolution) {
        if (wheelRpm < 0 || wheelRevolution < 0) {
            return false;
        }

        // スピードを計算する
        mSpeedKmh = (float) AppUtil.calcSpeedKmPerHour(wheelRpm, getWheelOuterLength());
        mMaxSpeedKmh = Math.max(mSpeedKmh, mMaxSpeedKmh);
        mWheelRevolution = wheelRevolution;
        mUpdatedTime = now();
        return true;
    }
}
