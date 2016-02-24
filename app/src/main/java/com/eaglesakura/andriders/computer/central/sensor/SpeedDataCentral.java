package com.eaglesakura.andriders.computer.central.sensor;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.computer.central.calculator.SpeedDataCalculator;
import com.eaglesakura.andriders.internal.protocol.ApplicationProtocol;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.internal.protocol.RawSensorData;
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
    private final long SENSOR_TIMEOUT_MS = 1000 * 30;

    /**
     * センサーからの更新時刻
     */
    private Timer mSensorTime;

    /**
     * センサー由来の現在速度
     */
    private double mSensorNowSpeed;

    /**
     * センサー由来のホイール回転数
     */
    private int mSensorWheelRevolution;

    /**
     * センサー由来の回転数
     */
    private float mSensorWheelRpm;

    /**
     * GPS由来の速度
     */
    private Timer mGpsSensorTime;

    /**
     * GPS由来の現在速度
     */
    private double mGpsNowSpeed;

    /**
     * デバイス接続されていればtrue
     */
    private boolean mConnected;

    /**
     * 速度計算機
     */
    private final SpeedDataCalculator mSpeedDataCalculator;

    private long mLastUpdatedTime;

    public SpeedDataCentral(SpeedDataCalculator speedDataCalculator) {
        super(SensorType.SpeedSensor);
        this.mSpeedDataCalculator = speedDataCalculator;
    }

    /**
     * 速度を取得する。
     * <p/>
     * この速度は基本的にBLE由来を返すが、もしタイムアウトしていたらGPS由来に自動的に切り替える
     */
    public double getSpeedKmh() {
        return Math.max(0, mSpeedDataCalculator.getSpeedKmh());
    }

    /**
     * 速度取得可能なデバイスに接続されている場合はtrue
     */
    public boolean isConnectedDevices() {
        return mConnected;
    }

    /**
     * 有効であればtrue
     */
    public boolean valid() {
        return (System.currentTimeMillis() - mLastUpdatedTime) < CentralDataManager.DATA_TIMEOUT_MS;
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
        mSensorNowSpeed = AceUtils.calcSpeedKmPerHour(wheelRpm, getSettings().getUserProfiles().getWheelOuterLength());
        mSensorWheelRevolution = wheelRevolution;

        if (mSensorTime == null) {
            mSensorTime = new Timer();
        } else {
            mSensorTime.start();
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
        if (mSensorTime != null) {
            if (mSensorTime.end() < SENSOR_TIMEOUT_MS) {
                // BLEセンサーがタイムアウトしていないのでBLEセンサーの速度で確定する
                mConnected = true;
                mSpeedDataCalculator.setSpeedKmh(mSensorNowSpeed);
                mLastUpdatedTime = mSensorTime.getStartTime();
                return;
            }

            // タイムアウトした
            LogUtil.log("SensorSpeed Timeout");
            mSensorTime = null;
            mSensorWheelRpm = 0;
            mSensorWheelRevolution = 0;
        }

        if (mGpsSensorTime != null) {
            if (mGpsSensorTime.end() < SENSOR_TIMEOUT_MS) {
                // GPSセンサーがタイムアウトしてないのでこっち
                mConnected = true;
                mSpeedDataCalculator.setSpeedKmh(mGpsNowSpeed);
                mLastUpdatedTime = mGpsSensorTime.getStartTime();
                return;
            }
        }

        // 両方のセンサーがタイムアウトしたので速度無効
        mSpeedDataCalculator.setSpeedKmh(0);
        mSensorWheelRpm = 0;
        mSensorWheelRevolution = 0;
        mConnected = false;
    }

    @Override
    public void buildData(CentralDataManager parent, RawCentralData result) {
        if (valid()) {
            RawSensorData.RawSpeed speed = new RawSensorData.RawSpeed();
            speed.date = mLastUpdatedTime;
            speed.speedKmPerHour = (float) mSpeedDataCalculator.getSpeedKmh();
            speed.zone = mSpeedDataCalculator.getSpeedZone();

            speed.wheelRevolution = mSensorWheelRevolution;
            speed.wheelRpm = mSensorWheelRpm;

            if (mSensorTime != null) {
                speed.flags |= RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_SENSOR;
            } else {
                speed.flags |= RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_GPS;
            }

            result.sensor.speed = speed;
            result.centralStatus.connectedFlags |= ApplicationProtocol.RawCentralStatus.CONNECTED_FLAG_SPEED_SENSOR;
        }
    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}

