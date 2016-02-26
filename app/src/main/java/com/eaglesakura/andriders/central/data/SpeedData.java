package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.central.data.base.BaseCalculator;
import com.eaglesakura.andriders.central.data.geo.GeoSpeedData;
import com.eaglesakura.andriders.central.data.scsensor.SensorSpeedData;
import com.eaglesakura.andriders.sensor.SpeedZone;

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

    public SpeedData(CycleClock clock, GeoSpeedData locationSpeedCalculator, SensorSpeedData sensorSpeedCalculator) {
        super(clock);
        mLocationSpeedCalculator = locationSpeedCalculator;
        mSensorSpeedCalculator = sensorSpeedCalculator;
    }

    /**
     * 現在の速度を取得する
     */
    public double getSpeedKmh() {
        if (mSensorSpeedCalculator.valid()) {
            return mSensorSpeedCalculator.getSpeedKmh();
        } else {
            return mLocationSpeedCalculator.getSpeedKmh();
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

}
