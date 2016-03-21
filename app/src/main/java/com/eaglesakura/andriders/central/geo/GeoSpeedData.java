package com.eaglesakura.andriders.central.geo;

import com.eaglesakura.andriders.central.Clock;
import com.eaglesakura.andriders.central.base.BaseCalculator;
import com.eaglesakura.geo.GeoUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Timer;

/**
 * 距離情報を計算する
 */
public class GeoSpeedData extends BaseCalculator {
    /**
     * 緯度
     */
    private double mLatitude;

    /**
     * 経度
     */
    private double mLongitude;

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

    public GeoSpeedData(Clock clock) {
        super(clock);
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    /**
     * データが信頼できる場合はtrue
     */
    public boolean valid() {
        return (now() - mUpdatedTime) < DATA_TIMEOUT_MS;
    }

    public long getUpdatedTime() {
        return mUpdatedTime;
    }

    /**
     * GPS由来の速度情報
     */
    public double getSpeedKmh() {
        if (valid()) {
            return mSpeedKmh;
        } else {
            return 0;
        }
    }

    /**
     * GPS由来の最高速度を取得する
     */
    public double getMaxSpeedKmh() {
        return mMaxSpeedKmh;
    }

    /**
     * 現在地点を更新する
     */
    public void setLocation(double lat, double lng) {
        final long timestamp = now();
        if (mUpdatedTime == 0) {
            mLatitude = lat;
            mLongitude = lng;
            mUpdatedTime = timestamp;
            return;
        }

        if (timestamp <= mUpdatedTime) {
            // 時刻エラー
            return;
        }

        // 前の地点からの差分を計算する
//        final double diffKiloMeter = location.distanceTo(beforeLocation) / 1000.0; // m -> km単位に落とす
        final double diffKiloMeter = GeoUtil.calcDistanceKiloMeter(lat, lng, mLatitude, mLongitude);

        // 差分時間から、1時間あたりの速度を求める
        final double diffTimeHour = Timer.msToHour(timestamp - mUpdatedTime);
        final double GEO_SPEED_KM_H = diffKiloMeter / diffTimeHour;
        if (GEO_SPEED_KM_H < 120) {
            // 時速120kmを超えたら恐らくGPS座標が飛んでいるため、計算をしてはいけない
            // 差分を足し込む
            mSpeedKmh = GEO_SPEED_KM_H;
        }
        LogUtil.log("diff move(%f km = %f m) time(%f sec) speed(%f km/h)",
                diffKiloMeter,
                diffKiloMeter * 1000,
                Timer.msToSec(timestamp - mUpdatedTime),
                GEO_SPEED_KM_H);

        // 位置を更新する
        mLatitude = lat;
        mLongitude = lng;
        mUpdatedTime = timestamp;
        mMaxSpeedKmh = Math.max(mMaxSpeedKmh, mSpeedKmh);
    }
}
