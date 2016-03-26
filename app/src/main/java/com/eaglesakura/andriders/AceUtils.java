package com.eaglesakura.andriders;

import com.google.android.gms.maps.model.LatLng;

import com.eaglesakura.andriders.serialize.RawGeoPoint;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.util.SerializeUtil;

/**
 *
 */
public class AceUtils {

    /**
     * ホイールの長さ設定
     */
    public static final int WHEEL_LENGTH_MAX = 2350;

    /**
     * ミリ秒単位の時間を適当なフォーマットにかけて文字列として返す
     *
     * @param milliSeconds 時刻のミリ秒
     */
    public static String formatTimeMilliSecToString(long milliSeconds) {
        int sessionTimeHour = (int) (milliSeconds / 1000 / 60 / 60);
        int sessionTimeMinute = (int) (milliSeconds / 1000 / 60) % 60;
        int sessionTimeSec = (int) (milliSeconds / 1000) % 60;
        if (sessionTimeHour == 0) {
            if (sessionTimeMinute == 0) {
                // 秒だけ
                return FrameworkCentral.getApplication().getString(R.string.Common_Formatter_Time_S, sessionTimeSec);
            } else {
                // 分秒
                return FrameworkCentral.getApplication().getString(R.string.Common_Formatter_Time_MS, sessionTimeMinute, sessionTimeSec);
            }
        } else {
            // 時分
            return FrameworkCentral.getApplication().getString(R.string.Common_Formatter_Time_HM, sessionTimeHour, sessionTimeMinute);
        }
    }


    /**
     * ホイール回転数と外周長から時速を算出する
     */
    public static double calcSpeedKmPerHour(double wheelRpm, double wheelOuterLength) {
        // 現在の1分間回転数から毎時間回転数に変換
        final double currentRpHour = wheelRpm * 60;
        // ホイールの外周mm/hに変換
        double moveLength = currentRpHour * wheelOuterLength;
        // mm => m => km
        moveLength /= (1000.0 * 1000.0);
        return moveLength;
    }


    public static LatLng toLatLng(RawGeoPoint geo) {
        if (geo == null) {
            return null;
        }
        return new LatLng(geo.latitude, geo.longitude);
    }


    /**
     * Serialize -> Deserializeを行うことで簡易cloneを行う
     */
    public static <T> T publicFieldClone(T origin) {
        try {
            return SerializeUtil.deserializePublicFieldObject((Class<T>) origin.getClass(), SerializeUtil.serializePublicFieldObject(origin, false));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
