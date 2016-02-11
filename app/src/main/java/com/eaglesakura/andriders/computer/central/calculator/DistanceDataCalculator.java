package com.eaglesakura.andriders.computer.central.calculator;

import com.eaglesakura.geo.GeoUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Timer;

/**
 * 距離情報を計算する
 */
public class DistanceDataCalculator extends BaseCalculator {
    /**
     * このセッションの移動距離（キロ単位）
     */
    double mSessionDistanceKm;

    /**
     * 前の計算地点
     */
    double mBeforeLatitude;

    double mBeforeLongitude;

    Timer mBeforeLocationTime = new Timer();


    /**
     * GPS由来の速度
     */
    double mGeoSpeedKmh;

    public DistanceDataCalculator() {
    }

    public double getSessionDistanceKm() {
        return mSessionDistanceKm;
    }

    /**
     * GPS由来の速度情報
     */
    public double getGeoSpeedKmh() {
        return mGeoSpeedKmh;
    }

    /**
     * 現在地点を更新する
     */
    public void updateLocation(double lat, double lng) {
        if (mBeforeLatitude == 0 && mBeforeLongitude == 0) {
            mBeforeLatitude = lat;
            mBeforeLongitude = lng;
            mBeforeLocationTime.start();
            return;
        }

        // 前の地点からの差分を計算する
//        final double diffKiloMeter = location.distanceTo(beforeLocation) / 1000.0; // m -> km単位に落とす
        final double diffKiloMeter = GeoUtil.calcDistanceKiloMeter(lat, lng, mBeforeLatitude, mBeforeLongitude);
        final long diffTimeMs = mBeforeLocationTime.end();

        // 差分時間から、1時間あたりの速度を求める
        double diffTimeHour = (double) diffTimeMs / 1000.0 / 60.0 / 60.0;
        double GEO_SPEED_KM_H = diffKiloMeter / diffTimeHour;
        if (GEO_SPEED_KM_H < 120) {
            // 時速120kmを超えたら恐らくGPS座標が飛んでいるため、計算をしてはいけない

            // 差分を足し込む
            mGeoSpeedKmh = GEO_SPEED_KM_H;
            mSessionDistanceKm += diffKiloMeter;
        }
        LogUtil.log("diff move(%f km = %f m) time(%f sec) speed(%f km/h)", diffKiloMeter, diffKiloMeter * 1000, (float) diffTimeMs / 1000.0f, GEO_SPEED_KM_H);

        // 位置を更新する
        mBeforeLatitude = lat;
        mBeforeLongitude = lng;
        mBeforeLocationTime.start();
    }
}
