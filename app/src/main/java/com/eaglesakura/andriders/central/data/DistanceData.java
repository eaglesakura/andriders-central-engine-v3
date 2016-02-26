package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.central.data.base.BaseCalculator;
import com.eaglesakura.util.Timer;

/**
 * 現在速度から走行距離を求める計算機
 */
public class DistanceData extends BaseCalculator {
    /**
     * 走行距離
     */
    private double mDistanceKm;

    public DistanceData(CycleClock clock) {
        super(clock);
    }

    /**
     * 走行距離（キロ）を取得する
     */
    public double getDistanceKm() {
        return mDistanceKm;
    }

    /**
     * 更新された
     */
    public void onUpdate(long diffTimeMs, double nowSpeedKmh) {
        if (diffTimeMs <= 0 || nowSpeedKmh <= 0) {
            return;
        }
        // 差分を取得する
        final double diffHour = Timer.msToHour(diffTimeMs);
        final double diffDistance = (diffHour * nowSpeedKmh);

        // 差分を加算してタイマーを再開する
        mDistanceKm += diffDistance;
    }
}
