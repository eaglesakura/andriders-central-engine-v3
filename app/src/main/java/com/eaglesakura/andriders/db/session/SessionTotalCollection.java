package com.eaglesakura.andriders.db.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class SessionTotalCollection {
    List<SessionTotal> mTotals = new ArrayList<>();

    SessionTotalCollection() {
    }

    void add(SessionTotal total) {
        mTotals.add(total);
    }

    public enum Order {
        /**
         * 昇順（古いものが最初）
         */
        Asc,

        /**
         * 降順（新しい物が最初）
         */
        Desc,
    }

    public double getSumDistanceKm() {
        double result = 0;
        for (SessionTotal total : mTotals) {
            result += total.getSumDistanceKm();
        }
        return result;
    }

    public double getSumAltitude() {
        double result = 0;
        for (SessionTotal total : mTotals) {
            result += total.getSumAltitude();
        }
        return result;
    }

    /**
     * 1日の最長到達距離を取得する
     */
    public double getLongestDateDistanceKm() {
        double result = 0;
        for (SessionTotal total : mTotals) {
            result = Math.max(result, total.getSumDistanceKm());
        }
        return result;
    }

    public double getMaxSpeedKmh() {
        double result = 0;
        for (SessionTotal total : mTotals) {
            result = Math.max(result, total.getMaxSpeedKmh());
        }
        return result;
    }

    public int getMaxCadence() {
        int result = 0;
        for (SessionTotal total : mTotals) {
            result = Math.max(result, total.getMaxCadence());
        }
        return result;
    }

    /**
     * 指定期間内の獲得エクササイズ値を取得する
     *
     * @param startTime 開始時刻
     * @param endTime   終了時刻
     * @return 合計エクササイズ
     */
    public double getRangeExercise(long startTime, long endTime) {
        double result = 0;
        for (SessionTotal total : mTotals) {
            if (total.getStartTime().getTime() >= startTime && total.getEndTime().getTime() <= endTime) {
                result += total.getExercise();
            }
        }
        return result;
    }

    /**
     * 指定期間内の消費カロリー値を取得する
     *
     * @param startTime 開始時刻
     * @param endTime   終了時刻
     * @return 合計カロリー(kcal)
     */
    public double getRangeCalorie(long startTime, long endTime) {
        double result = 0;
        for (SessionTotal total : mTotals) {
            if (total.getStartTime().getTime() >= startTime && total.getEndTime().getTime() <= endTime) {
                result += total.getCalories();
            }
        }
        return result;
    }

    public void sort(Order order) {
        if (order == Order.Asc) {
            Collections.sort(mTotals, (a, b) -> Long.compare(a.getStartTime().getTime(), b.getStartTime().getTime()));
        } else {
            Collections.sort(mTotals, (a, b) -> -(Long.compare(a.getStartTime().getTime(), b.getStartTime().getTime())));
        }
    }

    public List<SessionTotal> getTotals() {
        return mTotals;
    }
}
