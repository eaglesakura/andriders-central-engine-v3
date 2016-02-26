package com.eaglesakura.andriders.computer.central.sensor;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.computer.central.data.DistanceData;
import com.eaglesakura.andriders.computer.central.data.geo.GeoSpeedData;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.internal.protocol.RawSensorData;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.andriders.sensor.SpeedZone;
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

    enum SpeedSource {
        None,
        Sensor,
        Gps,
    }

    /**
     * どのセンサーで速度をとっているか
     */
    private SpeedSource mSpeedSource = SpeedSource.None;

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

    private long mLastUpdatedTime;

    /**
     * 距離計算
     */
    private final DistanceData mDistanceCalculator;

    public SpeedDataCentral(DistanceData distanceCalculator) {
        super(SensorType.SpeedSensor);
        mDistanceCalculator = distanceCalculator;
    }

    /**
     * 速度を取得する。
     * <p/>
     * この速度は基本的にBLE由来を返すが、もしタイムアウトしていたらGPS由来に自動的に切り替える
     */
    public double getSpeedKmh() {
        if (valid()) {
            switch (mSpeedSource) {
                case Sensor:
                    return mSensorNowSpeed;
                case Gps:
                    return mGpsNowSpeed;
            }
        }
        return 0;
    }

    /**
     * 速度取得可能なデバイスに接続されている場合はtrue
     */
    public boolean isConnectedDevices() {
        return valid() & mSpeedSource != SpeedSource.None;
    }

    /**
     * 有効であればtrue
     */
    public boolean valid() {
        return (System.currentTimeMillis() - mLastUpdatedTime) < CentralDataManager.DATA_TIMEOUT_MS;
    }

    /**
     * ホイールの外周サイズ（mm）を取得する
     */
    public float getWheelOuterLength() {
        return getSettings().getUserProfiles().getWheelOuterLength();
    }

    /**
     * センサー由来の速度を更新する
     *
     * @param wheelRpm        ホイール回転数
     * @param wheelRevolution ホイール回転数合計
     */
    public void setSensorSpeed(float wheelRpm, int wheelRevolution) {
        if (wheelRpm < 0 || wheelRevolution < 0) {
            return;
        }

        // スピードを計算する
        mSensorNowSpeed = AceUtils.calcSpeedKmPerHour(wheelRpm, getWheelOuterLength());
        mSensorWheelRevolution = wheelRevolution;

        if (mSensorTime == null) {
            mSensorTime = new Timer();
        } else {
            mSensorTime.start();
        }
    }

    /**
     * GPSセンサー由来の速度を更新する
     *
     * @param calc 位置からの速度計算
     */
    public void setGpsSpeed(GeoSpeedData calc) {
        mGpsNowSpeed = calc.getSpeedKmh();
        if (mGpsSensorTime == null) {
            mGpsSensorTime = new Timer();
        } else {
            mGpsSensorTime.start();
        }
    }

    /**
     * 速度を更新する
     */
    private void updateCurrentSpeed() {
        if (mSensorTime != null) {
            if (mSensorTime.end() < SENSOR_TIMEOUT_MS) {
                // BLEセンサーがタイムアウトしていないのでBLEセンサーの速度で確定する
                mSpeedSource = SpeedSource.Sensor;
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
                mSpeedSource = SpeedSource.Gps;
                mLastUpdatedTime = mGpsSensorTime.getStartTime();
                return;
            }
        }

        // 両方のセンサーがタイムアウトしたので速度無効
        mSensorWheelRpm = 0;
        mSensorWheelRevolution = 0;
        mSpeedSource = SpeedSource.None;
    }

    @Override
    public void onUpdate(CentralDataManager parent, long diffTimeMs) {
        updateCurrentSpeed();

    }

    @Override
    public void buildData(CentralDataManager parent, RawCentralData result) {
        if (valid()) {
            RawSensorData.RawSpeed speed = new RawSensorData.RawSpeed();
            speed.date = mLastUpdatedTime;
            speed.speedKmPerHour = (float) getSpeedKmh();
            speed.zone = getSpeedZone();

            speed.wheelRevolution = mSensorWheelRevolution;
            speed.wheelRpm = mSensorWheelRpm;

            if (mSensorTime != null) {
                speed.flags |= RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_SENSOR;
            } else {
                speed.flags |= RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_GPS;
            }

            result.sensor.speed = speed;
            result.centralStatus.connectedFlags |= RawCentralData.RawCentralStatus.CONNECTED_FLAG_SPEED_SENSOR;
        }
    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }

    /**
     * 速度ゾーンを取得する
     */
    public SpeedZone getSpeedZone() {
        final double speed = getSpeedKmh();
        if (speed < 8) {
            // 適当な閾値よりも下は停止とみなす
            return SpeedZone.Stop;
        } else if (speed < getSettings().getUserProfiles().getSpeedZoneCruise()) {
            // 巡航速度よりも下は低速度域
            return SpeedZone.Slow;
        } else if (speed < getSettings().getUserProfiles().getSpeedZoneSprint()) {
            // スプリント速度よりも下は巡航速度
            return SpeedZone.Cruise;
        } else {
            // スプリント速度を超えたらスプリント
            return SpeedZone.Sprint;
        }
    }

}

