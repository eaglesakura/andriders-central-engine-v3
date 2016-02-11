package com.eaglesakura.andriders.ble.cadence;

import com.eaglesakura.andriders.ble.SensorUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Timer;

import android.annotation.SuppressLint;

/**
 * ユーザーのケイデンス/スピード情報
 */
public class SpeedCadenceData {

    /**
     * 最終更新時間
     */
    private Timer lastUpdatedTime = new Timer();

    /**
     * センサー設定時間
     */
    private SensorTime sensorTime = new SensorTime();

    /**
     * クランク/ホイールの合計回転数
     */
    private int cumulativeCrankWheelRevolutions = 0;

    /**
     * 計算開始時点での回転数
     */
    private int startRevolutions = -1;

    /**
     * クラインク・ホイール回転のリセット回数
     */
    private int revolutionsResetCount = 0;

    /**
     * クランクの回転数
     */
    private double sensorRpm = 0;

    /**
     * チェック間隔の秒数
     */
    private double statusCheckIntervalMs = 2.0;

    /**
     * センサーのタイムアウト時間
     */
    private double sensorTimeoutIntervalMs = SensorUtil.SENSOR_TIMEOUT_MS;


    public SpeedCadenceData() {
    }

    /**
     * インターバル指定
     */
    public void setStatusCheckIntervalMs(double statusCheckInterval) {
        this.statusCheckIntervalMs = statusCheckInterval;
    }

    /**
     * センサーの更新インターバル
     */
    public double getStatusCheckIntervalMs() {
        return statusCheckIntervalMs;
    }

    /**
     * センサーのタイムアウト時間を指定する
     */
    public void setSensorTimeoutIntervalMs(double sensorTimeoutInterval) {
        this.sensorTimeoutIntervalMs = sensorTimeoutInterval;
    }

    /**
     * センサーのタイムアウト時間を取得する
     */
    public double getSensorTimeoutIntervalMs() {
        return sensorTimeoutIntervalMs;
    }

    /**
     * 合計回転数を取得する
     */
    public int getSumRevolutions() {
        if (startRevolutions < 0) {
            return 0;
        }

        return (0xFFFF * revolutionsResetCount) + cumulativeCrankWheelRevolutions - startRevolutions;
    }

    /**
     * 最後に更新された時刻を取得する
     */
    public long getLastUpdatedTime() {
        synchronized (this) {
            return lastUpdatedTime.getStartTime();
        }
    }

    /**
     * クランクの回転数を取得する
     */
    public double getRpm() {
        synchronized (this) {
            // 指定時間以上回転がなかったらケイデンスをリセットする
            if (lastUpdatedTime.end() > getSensorTimeoutIntervalMs()) {
                LogUtil.log("reset rpm");
                sensorRpm = 0;
            }

            return sensorRpm;
        }
    }

    /**
     * 最高速度を取得する
     *
     * @param wheelOuterLength ホイール外周サイズ（ミリメートル）
     */
    public double getSpeedKmPerHour(double wheelOuterLength) {
        // 現在の1分間回転数から毎時間回転数に変換
        final double currentRpHour = getRpm() * 60;
        // ホイールの外周mm/hに変換
        double moveLength = currentRpHour * wheelOuterLength;
        // mm => m => km
        moveLength /= (1000.0 * 1000.0);
        return moveLength;
    }

    /**
     * クランクの回転数を取得する
     */
    public int getRpmInt() {
        return (int) getRpm();
    }

    void abortUpdate(int cumulativeCrankWheelRevolutions, int sensorUpdatedTime) {
        this.cumulativeCrankWheelRevolutions = cumulativeCrankWheelRevolutions;
        lastUpdatedTime.start();
        sensorTime.reset(sensorUpdatedTime);
    }

    /**
     * @return 更新が行われたらtrue
     */
    @SuppressLint("DefaultLocale")
    public boolean update(int cumulativeCrankWheelRevolutions, int sensorUpdatedTime) {
        synchronized (this) {

            final double oldSensorRPM = this.sensorRpm;

            // ケイデンス/ホイール値に変化が無かったら何もしない
            if (this.cumulativeCrankWheelRevolutions == cumulativeCrankWheelRevolutions) {
                return false;
            }

            // 開始時点での回転数を設定
            if (startRevolutions < 0) {
                startRevolutions = cumulativeCrankWheelRevolutions;
            }

            // ケイデンス/ホイールが減ったら一周りしたから値の更新だけして何もしない
            if (cumulativeCrankWheelRevolutions < this.cumulativeCrankWheelRevolutions) {
                abortUpdate(cumulativeCrankWheelRevolutions, sensorUpdatedTime);
                ++revolutionsResetCount;
                SensorUtil.d("overflow cadence value");
                return true;
            }

            // オフセット値を取得する
            double offsetRevo = cumulativeCrankWheelRevolutions - this.cumulativeCrankWheelRevolutions;
            double offsetTimeMs = sensorTime.offsetTimeMs(sensorUpdatedTime);

            SensorUtil.d(String.format("offsetRevo(%f) offsetTimeMs(%f) ", offsetRevo, offsetTimeMs));

            // 指定秒経過していなかったら時間単位の精度が低いため何もしない
            if (offsetTimeMs < (double) getStatusCheckIntervalMs()) {
                SensorUtil.d("interval not :: " + (offsetTimeMs / 1000.0f));
                return false;
            }

            // オフセット時間が一定を超えていたら計算出来ない
            if (offsetTimeMs > getSensorTimeoutIntervalMs()) {
                SensorUtil.d("error over time :: " + (offsetTimeMs / 1000.0) + " sec");
                abortUpdate(cumulativeCrankWheelRevolutions, sensorUpdatedTime);
                return true;
            }

            // 計算する
            {
                // 1minuteが基本
                double mult = (1000.0 * 60.0) / offsetTimeMs;
                offsetRevo *= mult;

                // 指定時間に行われた回転数から、1分間の回転数を求める
                sensorRpm = offsetRevo;

                if (sensorRpm > 600) {
                    // 何らかのエラー
                    // 600RPMを超えると、ロードバイクなら時速220kmを超えることになり、世界記録を超えてしまうためこれはエラーと判断出来る
                    sensorRpm = oldSensorRPM;
                }
            }

            // 上書きを行う
            abortUpdate(cumulativeCrankWheelRevolutions, sensorUpdatedTime);

            return true;
        }
    }
}
