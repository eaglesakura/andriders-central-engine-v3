package com.eaglesakura.andriders.central;

import com.eaglesakura.andriders.central.base.BaseCalculator;
import com.eaglesakura.andriders.central.geo.GeoSpeedData;
import com.eaglesakura.andriders.central.scsensor.SensorSpeedData;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.andriders.sensor.SpeedZone;
import com.eaglesakura.andriders.util.Clock;

/**
 * GPSやセンサーの速度を統括し、適度な情報を取得する
 */
public class SpeedData extends BaseCalculator {

    /**
     * GPS由来の速度
     */
    private final GeoSpeedData mLocationSpeedCalculator;

    /**
     * センサー由来の速度
     */
    private final SensorSpeedData mSensorSpeedCalculator;

    public enum SpeedSource {
        None {
            @Override
            public int getFlag() {
                return 0;
            }
        },
        Sensor {
            @Override
            public int getFlag() {
                return RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_SENSOR;
            }
        },
        GPS {
            @Override
            public int getFlag() {
                return RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_GPS;
            }
        };

        public abstract int getFlag();
    }

    public SpeedData(Clock clock, GeoSpeedData geoSpeedCalculator, SensorSpeedData sensorSpeedCalculator) {
        super(clock);
        mLocationSpeedCalculator = geoSpeedCalculator;
        mSensorSpeedCalculator = sensorSpeedCalculator;
    }

    /**
     * 速度由来を取得する
     */
    public SpeedSource getSource() {
        if (mSensorSpeedCalculator.valid()) {
            return SpeedSource.Sensor;
        } else if (mLocationSpeedCalculator.valid()) {
            return SpeedSource.GPS;
        } else {
            return SpeedSource.None;
        }
    }

    /**
     * 現在の速度を取得する
     */
    public double getSpeedKmh() {
        if (mSensorSpeedCalculator.valid()) {
            return mSensorSpeedCalculator.getSpeedKmh();
        } else if (mLocationSpeedCalculator.valid()) {
            return mLocationSpeedCalculator.getSpeedKmh();
        } else {
            return 0;
        }
    }

    /**
     * 最高速度を取得する。
     *
     * MEMO: センサーごとに分離して最高速度は保持しているが、接続されているセンサーの値を優先して取得する。
     * ユースケースとしてはGPS OFFで室内トレーニングが考えられるため、センサーが反応しないかつGPSが有効のみGPS最高速度を取り出すことになる。
     */
    public double getMaxSpeedKmh() {
        if (!mSensorSpeedCalculator.valid() && mLocationSpeedCalculator.valid()) {
            return mLocationSpeedCalculator.getMaxSpeedKmh();
        } else {
            return mSensorSpeedCalculator.getMaxSpeedKmh();
        }
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


    /**
     * センサー情報を取得する
     *
     * @return センサー情報を書き込んだ場合true
     */
    public boolean getSensor(RawSensorData dstSensor) {
        SpeedSource speedSrc = getSource();
        if (speedSrc == SpeedSource.None) {
            // 速度が取得できない
            return false;
        }

        dstSensor.speed = new RawSensorData.RawSpeed();
        dstSensor.speed.flags |= speedSrc.getFlag();
        dstSensor.speed.speedKmPerHour = (float) getSpeedKmh();
        dstSensor.speed.zone = getSpeedZone();

        if (speedSrc == SpeedSource.GPS) {
            dstSensor.speed.date = mLocationSpeedCalculator.getUpdatedTime();
        } else {
            dstSensor.speed.date = mSensorSpeedCalculator.getUpdatedTime();
            dstSensor.speed.wheelRpm = mSensorSpeedCalculator.getWheelRpm();
            dstSensor.speed.wheelRevolution = mSensorSpeedCalculator.getWheelRevolution();
        }

        return true;
    }
}
