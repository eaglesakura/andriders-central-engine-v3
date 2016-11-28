package com.eaglesakura.andriders.central.data.session;

import com.eaglesakura.andriders.central.data.base.BaseCalculator;
import com.eaglesakura.andriders.util.Clock;

/**
 * セッションの時刻情報を管理する
 */
public class SessionTime extends BaseCalculator {
    /**
     * 合計自走時間
     */
    private long mActiveTimeMs;

    /**
     * 合計自走距離
     */
    private double mActiveDistanceKm;

    /**
     * 開始時刻
     */
    private long mStartDate;

    /**
     * セッションを生成する
     */
    public SessionTime(Clock clock) {
        super(clock);
        mStartDate = clock.now();
    }

    /**
     * セッション開始時刻を取得する
     */
    public long getStartDate() {
        return mStartDate;
    }

    /**
     * セッション期間をミリ秒単位で取得する
     */
    public long getSessionDurationMs() {
        return now() - mStartDate;
    }

    /**
     * 自走時間をミリ秒単位で取得する
     */
    public long getActiveTimeMs() {
        return mActiveTimeMs;
    }

    /**
     * 自走時間をkm単位で取得する
     */
    public double getActiveDistanceKm() {
        return mActiveDistanceKm;
    }

    /**
     * 自走時間を追加する
     */
    public void addActiveTimeMs(long ms) {
        mActiveTimeMs += ms;
    }

    /**
     * 自走距離を追加する
     */
    public void addActiveDistanceKm(double distanceKm) {
        mActiveDistanceKm += distanceKm;
    }
}
