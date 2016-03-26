package com.eaglesakura.andriders.ble.hw.cadence;

import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.AppLog;

/**
 * ユーザーのケイデンス/スピード情報
 */
public class SpeedCadenceSensorData {


    /**
     * 最終更新時刻
     */
    private long mUpdatedTime;

    /**
     * 最後に取得した合計回転数
     */
    private int mLastUpdatedRevolveCount = -1;

    /**
     * 最後に取得したセンサー時間
     */
    private int mLastUpdatedSensorTime = -1;

    /**
     * 初回取得時の回転数
     */
    private int mStartRevolveCount = -1;

    /**
     * 回転のリセット回数
     */
    private int mRevolvingOverflowCount = 0;

    /**
     * 回転数 / 分
     */
    private double mRpm = 0;

    /**
     * 現在時刻チェック時計
     */
    private final Clock mClock;

    /**
     * センサーのタイムアウト時間
     */
    private final int mSensorTimeoutMs;

    /**
     * チェック間隔の秒数
     */
    private final int mStatusCheckIntervalMs;

    public SpeedCadenceSensorData(Clock clock, int sensorTimeoutIntervalMs, int statusCheckIntervalMs) {
        mClock = clock;
        this.mSensorTimeoutMs = sensorTimeoutIntervalMs;
        this.mStatusCheckIntervalMs = statusCheckIntervalMs;
    }

    /**
     * 合計回転数を取得する
     */
    public int getSumRevolveCount() {
        if (mLastUpdatedRevolveCount < 0 || mStartRevolveCount < 0) {
            return 0;
        }

        return ((0x00010000 * mRevolvingOverflowCount) + mLastUpdatedRevolveCount) - mStartRevolveCount;
    }

    /**
     * データが有効であればtrue
     */
    public boolean valid() {
        return mClock.absDiff(mUpdatedTime) < mSensorTimeoutMs;
    }

    /**
     * クランクの回転数を取得する
     */
    public double getRpm() {
        if (valid()) {
            return mRpm;
        } else {
            return 0;
        }
    }

    /**
     * @param sensorRevolveCount センサーから取得した合計回転数
     * @param sensorUpdatedTime  センサー時刻(1/1024秒単位で刻まれる)
     * @return 更新が行われたらtrue
     */
    public boolean update(int sensorRevolveCount, int sensorUpdatedTime) {
        sensorRevolveCount &= BleSpeedCadenceUtil.SENSOR_16BIT_MASK;
        sensorUpdatedTime &= BleSpeedCadenceUtil.SENSOR_16BIT_MASK;

        if (mLastUpdatedRevolveCount < 0 || mStartRevolveCount < 0) {
            mStartRevolveCount = sensorRevolveCount;
            mLastUpdatedRevolveCount = sensorRevolveCount;
            mLastUpdatedSensorTime = sensorUpdatedTime;
            return false;
        }

        final double oldSensorRPM = getRpm();   // 古いRPM

        // 回転差分と時間差分を取得する
        final int offsetRevolve = BleSpeedCadenceUtil.get16bitOffset(mLastUpdatedRevolveCount, sensorRevolveCount);
        final int offsetTimeMs = (int) (BleSpeedCadenceUtil.sensorTimeToSeconds(BleSpeedCadenceUtil.get16bitOffset(mLastUpdatedSensorTime, sensorUpdatedTime)) * 1000.0);

        // 指定秒経過していなかったら時間単位の精度が低いため何もしない
        if (offsetTimeMs < mStatusCheckIntervalMs) {
//            AppLog.cadence("abort interval (%d ms < %d ms)", offsetTimeMs, mStatusCheckIntervalMs);
            return false;
        }
        AppLog.bleData(String.format("Revolve(+%d) Time(+%d ms) ", offsetRevolve, offsetTimeMs));


        // 計算する
        {
            // 1minuteが基本
            final double mult = (1000.0 * 60.0) / (double) offsetTimeMs;

            // 指定時間に行われた回転数から、1分間の回転数を求める
            mRpm = (double) offsetRevolve * mult;

            if (mRpm > 600) {
                // 何らかのエラー
                // 600RPMを超えると、ロードバイクなら時速220kmを超えることになり、世界記録を超えてしまうためこれはエラーと判断出来る
                mRpm = oldSensorRPM;
            }
        }

        // 値を上書きする
        if (BleSpeedCadenceUtil.is16bitOverflow(mLastUpdatedRevolveCount, sensorRevolveCount)) {
            // 回転数がオーバーフローしたので、カウンタを回す
            ++mRevolvingOverflowCount;
        }
        mLastUpdatedRevolveCount = sensorRevolveCount;
        mLastUpdatedSensorTime = sensorUpdatedTime;
        mUpdatedTime = mClock.now();
        return true;
    }
}
