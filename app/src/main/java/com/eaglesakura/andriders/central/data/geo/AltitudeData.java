package com.eaglesakura.andriders.central.data.geo;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.central.data.CycleClock;
import com.eaglesakura.andriders.central.data.base.BaseCalculator;
import com.eaglesakura.andriders.internal.protocol.RawGeoPoint;
import com.eaglesakura.geo.GeoUtil;
import com.eaglesakura.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 高度情報をチェックする
 */
public class AltitudeData extends BaseCalculator {

    /**
     * エラーとして許容する回数
     */
    private static final int MAX_ERROR_NUM = 20;

    /**
     * エラーとして認識する差分距離
     */
    private static final double ERROR_ALTITUDE_DIFF_METER = 30;

    /**
     * 下降に転じたと認識する高度
     */
    private static final double DECHAIN_ALTITUDE_DIFF_METER = 10;

    /**
     * 高度計算を行うために必要な地点数
     * <br>
     * 数が多いほど正確。ヒルクライム中は速度が落ちるため、追従速度よりも精度を優先する
     */
    private static final int ALTITUDE_CALC_AVARAGE_NUM = 10;

    /**
     * 勾配を計算するためのログ保存数
     */
    private static final int INCLINATION_CALC_NUM = 20;

    /**
     * 獲得標高のインターバールメートル
     * <br>
     * 余り大きいと境界で何度も獲得出来てしまう
     */
    private static final int SUM_ALTITUDE_INTERVAL_METER = 10;

    /**
     * 高度情報を含んだ地点
     */
    private List<RawGeoPoint> mAltPoints = new ArrayList<>();

    /**
     * 地点ログリスト
     */
    private List<RawGeoPoint> mCalcPointLogs = new ArrayList<>();


    /**
     * 計算時点での最高高度
     */
    private RawGeoPoint mMaxAltitudePoint;

    /**
     * 計算時点での最低高度
     */
    private RawGeoPoint mMinAltitudePoint;

    /**
     * 現在の標高
     */
    private double mCurrentAltitude = -1;

    /**
     * 今日の合計獲得標高
     */
    private double mSumAltitude = 0;

    /**
     * 傾斜％
     */
    private double mInclinationPercent;

    /**
     * 道の状態管理
     */
    private RoadState mRoadState = new RoadState();

    public AltitudeData(CycleClock clock) {
        super(clock);
    }

    /**
     * 高度情報を持っている場合はtrue
     */
    public boolean hasAltitude() {
        synchronized (this) {
            return mCurrentAltitude > 0;
        }
    }

    /**
     * 現在の標高をメートル単位で取得する
     */
    public double getCurrentAltitudeMeter() {
        synchronized (this) {
            return Math.max(0, mCurrentAltitude);
        }
//        return currentPoint.getAltitudeMeter();
    }

    public RawGeoPoint getMaxAltitudePoint() {
        return mMaxAltitudePoint;
    }

    public RawGeoPoint getMinAltitudePoint() {
        return mMinAltitudePoint;
    }

    public double getSessionSumAltitude() {
        return mSumAltitude + mRoadState.getClimgUpAltitude();
    }

    /**
     * 傾斜を取得する
     * <br>
     * 斜度は常識的な範囲内 ±40%として計算される
     */
    public double getInclinationPercent() {
        return MathUtil.minmax(-30, 30, mInclinationPercent);
    }


    /**
     * 傾斜角％を取得する
     */
    private double calcInclinationPercent() {
        if (mCalcPointLogs.size() < 2) {
            // 基準点が無ければ計算不可能
            return 0;
        }

        // ログ内で進んだ距離を合計する
        double distanceMeter = 0;
        for (int i = 0; i < (mCalcPointLogs.size() - 1); ++i) {
            RawGeoPoint before = mCalcPointLogs.get(i);
            RawGeoPoint current = mCalcPointLogs.get(i + 1);
            distanceMeter +=
                    GeoUtil.calcDistanceKiloMeter(current.latitude, current.longitude, before.latitude, before.longitude) * 1000;
        }

        if (distanceMeter <= 10) {
            // 距離が離れていなければ斜度0として返す
//            return mInclinationPercent;
            return 0;
        } else {
            double altitudeMeter = (getCurrentAltitudeMeter() - mCalcPointLogs.get(0).altitude);
            double result = (altitudeMeter / distanceMeter) * 100.0;
            return result;
        }
    }

    /**
     * 位置を更新した
     */
    public void setLocation(double lat, double lng, double alt) {
        if (((int) alt) <= 1) {
            return;
        }

        // 位置を更新する
        RawGeoPoint gp = AceUtils.toGeoPoint(lat, lng, alt);
        mAltPoints.add(gp);
        if (mAltPoints.size() > ALTITUDE_CALC_AVARAGE_NUM) {
            // 計算に不要な地点は捨てる
            mAltPoints.remove(0);
        }

        // 標高を計算する
        mCurrentAltitude = 0;
        for (RawGeoPoint p : mAltPoints) {
            mCurrentAltitude += p.altitude;
        }
        mCurrentAltitude /= mAltPoints.size();

        // 計算のために精度を下げる
        mCurrentAltitude = (int) (mCurrentAltitude + 0.5);

        // 計算済み地点ログを追加する
        RawGeoPoint calcCurrent = AceUtils.toGeoPoint(lat, lng, mCurrentAltitude);
        mCalcPointLogs.add(calcCurrent);
        if (mCalcPointLogs.size() > INCLINATION_CALC_NUM) {
            mCalcPointLogs.remove(0);
        }

        // 前の高度を持っていたら上昇分の計算を行う
        // 獲得標高をチェックする
        mRoadState.onUpdateAltitude(mCurrentAltitude);

        // 最高/最低地点チェック
        if (mMaxAltitudePoint == null || mCurrentAltitude > mMaxAltitudePoint.altitude) {
            mMaxAltitudePoint = calcCurrent;
        }
        if (mMinAltitudePoint == null || mCurrentAltitude < mMinAltitudePoint.altitude) {
            mMinAltitudePoint = calcCurrent;
        }

        // 傾斜を計算
        mInclinationPercent = calcInclinationPercent();
    }

    /**
     * 標高差の記録を行う
     */
    private class RoadState {

        /**
         * 基準地点
         */
        double minAltitude = -1;

        double maxAltitude = -1;

        double lastAltitude = -1;

        /**
         * ヒルクライム中だったらtrue
         */
        boolean climb = false;

        /**
         * 残り許容数
         */
        int errors = MAX_ERROR_NUM;

        /**
         * このステートでの合計
         */
        double getClimgUpAltitude() {
            if (!climb || (int) minAltitude <= 0 || (int) maxAltitude <= 0) {
                return 0;
            }

            return maxAltitude - minAltitude;
        }

        /**
         * ステータスをリセットする
         */
        private void reset(double currentAltitude) {
            if (climb) {
                mSumAltitude += getClimgUpAltitude();
                climb = false;
            }

            errors = MAX_ERROR_NUM;
            minAltitude = maxAltitude = lastAltitude = -1;
        }

        /**
         * 獲得標高を更新する
         */
        void onUpdateAltitude(double currentAltitude) {
            // 計算のために精度を下げる
            currentAltitude = (((int) (currentAltitude + 0.5)) / SUM_ALTITUDE_INTERVAL_METER) * SUM_ALTITUDE_INTERVAL_METER;

            if (lastAltitude < 0) {
                // 初回データを入力する
                lastAltitude = currentAltitude;
                minAltitude = currentAltitude;
                maxAltitude = currentAltitude;
            }

            if (Math.abs(currentAltitude - lastAltitude) > ERROR_ALTITUDE_DIFF_METER) {
                // 最後に受け取った標高との差分が許容値を超えていたらエラーとして扱う
                --errors;

                if (errors < 0) {
                    // エラーが許容値を超えたら、実際にその標高であると認めてあげる
                    reset(currentAltitude);
                }
                return;
            } else {
                // エラーじゃなかったらエラー許容をリセットする
                errors = MAX_ERROR_NUM;
            }
            if (climb && (maxAltitude - currentAltitude) > DECHAIN_ALTITUDE_DIFF_METER) {
                // 最高標高よりもある程度さがったらリセットをかける
                reset(currentAltitude);
            } else if (currentAltitude < minAltitude) {
                // 最低高度よりも下がったらリセットをかける
                reset(currentAltitude);
            } else if (currentAltitude > maxAltitude) {
                // 最高標高を超えたら上昇モードに切り替える
                climb = true;
                maxAltitude = currentAltitude;
            }
        }
    }

}
