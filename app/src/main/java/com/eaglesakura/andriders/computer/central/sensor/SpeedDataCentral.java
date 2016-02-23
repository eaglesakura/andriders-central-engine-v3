package com.eaglesakura.andriders.computer.central.sensor;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.computer.central.calculator.SpeedDataCalculator;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Timer;

/**
 * 速度情報はGPSとS&Cセンサーの二箇所から情報集約される
 */
public class SpeedDataCentral extends SensorDataCentral {
    /**
     * 一定時間以上BLEセンサーが更新されなければ、GPS由来のデータに変更する
     */
    final long SENSOR_TIMEOUT_MS = 1000 * 30;

    /**
     * BLEセンサーからの更新時刻
     */
    Timer mBleSensorTime;

    /**
     * BLE由来の現在速度
     */
    double mBleNowSpeed;

    /**
     * BLE由来のホイール回転数
     */
    int mBleWheelRevolution;

    /**
     * GPS由来の速度
     */
    Timer mGpsSensorTime;

    /**
     * GPS由来の現在速度
     */
    double mGpsNowSpeed;

    /**
     * デバイス接続されていればtrue
     */
    boolean mConnected;

    /**
     * 速度計算機
     */
    final SpeedDataCalculator speedDataCalculator;

    public SpeedDataCentral(SpeedDataCalculator speedDataCalculator) {
        super(SensorType.SpeedSensor);
        this.speedDataCalculator = speedDataCalculator;
    }

    /**
     * 速度を取得する。
     * <p/>
     * この速度は基本的にBLE由来を返すが、もしタイムアウトしていたらGPS由来に自動的に切り替える
     */
    public double getSpeedKmh() {
        return Math.max(0, speedDataCalculator.getSpeedKmh());
    }

    /**
     * 速度取得可能なデバイスに接続されている場合はtrue
     */
    public boolean isConnectedDevices() {
        return mConnected;
    }

    /**
     * BLEセンサー由来の速度を更新する
     *
     * @param wheelRpm        ホイール回転数
     * @param wheelRevolution ホイール回転数合計
     */
    public void setBleSensorSpeed(float wheelRpm, int wheelRevolution) {
        if (wheelRpm < 0 || wheelRevolution < 0) {
            return;
        }

        // スピードを計算する
        mBleNowSpeed = AceUtils.calcSpeedKmPerHour(wheelRpm, getSettings().getUserProfiles().getWheelOuterLength());
        mBleWheelRevolution = wheelRevolution;

        if (mBleSensorTime == null) {
            mBleSensorTime = new Timer();
        } else {
            mBleSensorTime.start();
        }
    }

    /**
     * GPSセンサー由来の速度を更新する
     */
    public void setGpsSensorSpeed(double speedKmh) {
        if (speedKmh < 0) {
            return;
        }

        mGpsNowSpeed = speedKmh;
        if (mGpsSensorTime == null) {
            mGpsSensorTime = new Timer();
        } else {
            mGpsSensorTime.start();
        }
    }

    @Override
    public void onUpdate(CentralDataManager parent) {
        if (mBleSensorTime != null) {
            if (mBleSensorTime.end() < SENSOR_TIMEOUT_MS) {
                // BLEセンサーがタイムアウトしていないのでBLEセンサーの速度で確定する
                mConnected = true;
                speedDataCalculator.setSpeedKmh(mBleNowSpeed);
                return;
            }

            // タイムアウトした
            LogUtil.log("BleSpeed Timeout");
            mBleSensorTime = null;
        }

        if (mGpsSensorTime != null) {
            if (mGpsSensorTime.end() < SENSOR_TIMEOUT_MS) {
                // GPSセンサーがタイムアウトしてないのでこっち
                mConnected = true;
                speedDataCalculator.setSpeedKmh(mGpsNowSpeed);
                return;
            }
        }

        // 両方のセンサーがタイムアウトしたので速度無効
        speedDataCalculator.setSpeedKmh(0);
        mConnected = false;
    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}

