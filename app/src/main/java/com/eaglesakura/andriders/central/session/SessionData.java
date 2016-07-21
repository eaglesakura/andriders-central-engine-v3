package com.eaglesakura.andriders.central.session;

import com.eaglesakura.andriders.central.base.BaseCalculator;
import com.eaglesakura.andriders.util.Clock;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * セッション情報を構築する
 */
public class SessionData extends BaseCalculator {

    /**
     * セッション識別子
     */
    private final String mSessionId;

    /**
     * 開始時刻
     */
    private final long mStartDate;

    /**
     * 合計自走時間
     */
    private long mActiveTimeMs;

    /**
     * 合計自走距離
     */
    private double mActiveDistanceKm;

    private static final SimpleDateFormat SESSION_KEY_FORMAT = new SimpleDateFormat("yyyyMMdd.HH.mm.ss.SS");


    /**
     * セッションを生成する
     *
     * @param startDate 開始時刻
     */
    public SessionData(Clock clock, long startDate) {
        super(clock);
        mStartDate = startDate;
        mSessionId = String.format("ssn.%s", SESSION_KEY_FORMAT.format(new Date(startDate)));
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
    public long getSessionDulationMs() {
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
     * セッション情報
     */
    public String getSessionId() {
        return mSessionId;
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
