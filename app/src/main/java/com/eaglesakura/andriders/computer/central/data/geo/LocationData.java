package com.eaglesakura.andriders.computer.central.data.geo;

import com.eaglesakura.andriders.computer.central.data.CycleClock;
import com.eaglesakura.andriders.computer.central.data.base.BaseCalculator;
import com.eaglesakura.util.LogUtil;

/**
 * 位置情報を統括する
 */
public class LocationData extends BaseCalculator {
    /**
     * 位置精度
     */
    private double mAccuracy;

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

    public LocationData(CycleClock clock, GeoSpeedData geoSpeedData) {
        super(clock);
        mAltitudeData = new AltitudeData(clock);
        mGeoSpeedData = geoSpeedData;
    }

    /**
     * 位置情報が有効ならtrue
     */
    public boolean valid() {
        return mUpdatedTime != 0;
    }

    /**
     * 精度をメートル単位で取得する
     */
    public double getAccuracyMeter() {
        return mAccuracy;
    }

    /**
     * 精度をチェックし、信頼できるならばtrueを返却する
     */
    public boolean isAccuracy(double accuracyMeter) {
        return accuracyMeter < 150;
    }

    /**
     * 位置情報を更新する
     *
     * @param timestamp     センサー時刻
     * @param lat           緯度
     * @param lng           経度
     * @param alt           高度(クラス内部で適度に補正される)
     * @param accuracyMeter メートル単位の精度
     */
    public boolean setLocation(long timestamp, double lat, double lng, double alt, double accuracyMeter) {
        if (!isAccuracy(accuracyMeter)) {
            // 信頼出来ないデータなのでdropする
            LogUtil.log("Drop GPS lat(%f) lng(%f), alt(%f) acc(%f)", lat, lng, alt, accuracyMeter);
            return false;
        }

        mAltitudeData.setLocation(lat, lng, alt);
        mGeoSpeedData.setLocation(timestamp, lat, lng);

        mRawAltitude = alt;
        mAccuracy = accuracyMeter;
        mUpdatedTime = timestamp;

        return true;
    }
}
