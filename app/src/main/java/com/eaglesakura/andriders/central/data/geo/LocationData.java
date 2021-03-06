package com.eaglesakura.andriders.central.data.geo;

import com.eaglesakura.andriders.central.data.base.BaseCalculator;
import com.eaglesakura.andriders.central.data.sensor.GeoSpeedData;
import com.eaglesakura.andriders.sensor.InclinationType;
import com.eaglesakura.andriders.serialize.RawLocation;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;

/**
 * 位置情報を統括する
 */
public class LocationData extends BaseCalculator {
    /**
     * 位置精度
     */
    private double mAccuracy = 500;

    /**
     * 高度情報
     */
    private final AltitudeData mAltitudeData;

    /**
     * 補正を行わない直値の高度情報
     */
    private double mRawAltitude;

    /**
     * 位置情報から計算された速度情報
     */
    private final GeoSpeedData mGeoSpeedData;

    /**
     * 更新時刻
     */
    private long mUpdatedTime;

    /**
     * ユーザーが設定した信頼精度
     */
    private final float mUserAccuracyMeter;

    public LocationData(Clock clock, GeoSpeedData geoSpeedData, float userAccuracyMeter) {
        super(clock);
        mAltitudeData = new AltitudeData(clock);
        mGeoSpeedData = geoSpeedData;
        mUserAccuracyMeter = userAccuracyMeter;
    }

    /**
     * 位置情報が有効ならtrue
     */
    public boolean valid() {
        return mUpdatedTime != 0;
    }

    public long getUpdatedTime() {
        return mUpdatedTime;
    }

    public double getLongitude() {
        return mGeoSpeedData.getLongitude();
    }

    public double getLatitude() {
        return mGeoSpeedData.getLatitude();
    }

    /**
     * 精度をメートル単位で取得する
     */
    public double getAccuracyMeter() {
        return mAccuracy;
    }

    /**
     * 現在の高度（メートル）を取得する
     */
    public double getAltitudeMeter() {
        return mAltitudeData.getCurrentAltitudeMeter();
    }

    /**
     * 獲得標高（メートル）を取得する
     */
    public double getSumAltitude() {
        return mAltitudeData.getSumAltitude();
    }

    /**
     * 現在の傾斜％を取得する
     */
    public double getInclinationPercent() {
        if (valid()) {
            return mAltitudeData.getInclinationPercent();
        } else {
            return 0;
        }
    }


    /**
     * 傾斜レベルを取得する
     */
    public InclinationType getInclinationType() {
        final double absInclination = Math.abs(getInclinationPercent());
        if (absInclination < 4) {
            // ゆるい傾斜は平坦として扱う
            return InclinationType.None;
        } else if (absInclination < 8) {
            // そこそこの坂はそこそこである。
            return InclinationType.Hill;
        } else {
            // ある程度を超えた傾斜は激坂として扱う
            return InclinationType.IntenseHill;
        }
    }

    /**
     * 精度をチェックし、信頼できるならばtrueを返却する
     */
    public boolean isReliance() {
        return isReliance(mAccuracy);
    }

    /**
     * 事前指定された信頼度よりも高精度であればtrue
     */
    private boolean isReliance(double accuracyMeter) {
        return accuracyMeter <= mUserAccuracyMeter;
    }

    /**
     * 位置情報を更新する
     *
     * @param lat           緯度
     * @param lng           経度
     * @param alt           高度(クラス内部で適度に補正される)
     * @param accuracyMeter メートル単位の精度
     */
    public boolean setLocation(double lat, double lng, double alt, double accuracyMeter) {
        if (!isReliance(accuracyMeter)) {
            // 信頼出来ないデータなのでdropする
            AppLog.gps("Drop GPS lat(%f) lng(%f), alt(%f) acc(%f)", lat, lng, alt, accuracyMeter);
            return false;
        }
        mAltitudeData.setLocation(lat, lng, alt);
        mGeoSpeedData.setLocation(lat, lng);

        mRawAltitude = alt;
        mAccuracy = accuracyMeter;
        mUpdatedTime = now();

        AppLog.gps("GPS lat(%f) lng(%f), alt(%f) acc(%f)", lat, lng, getAltitudeMeter(), accuracyMeter);
        return true;
    }


    /**
     * センサー情報を取得する
     *
     * @return センサー情報を書き込んだ場合true
     */
    public boolean getSensor(RawSensorData dstSensor) {
        if (!valid()) {
            return false;
        }
        dstSensor.location = new RawLocation();
        dstSensor.location.date = mUpdatedTime;
        dstSensor.location.inclinationPercent = (float) mAltitudeData.getInclinationPercent();
        dstSensor.location.inclinationType = getInclinationType();
        dstSensor.location.locationAccuracy = (float) mAccuracy;
        dstSensor.location.locationReliance = isReliance(mAccuracy);
        dstSensor.location.altitude = getAltitudeMeter();
        dstSensor.location.latitude = mGeoSpeedData.getLatitude();
        dstSensor.location.longitude = mGeoSpeedData.getLongitude();
        return true;
    }
}
