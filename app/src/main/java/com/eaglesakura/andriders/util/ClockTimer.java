package com.eaglesakura.andriders.util;

import com.eaglesakura.util.Timer;

import android.support.annotation.NonNull;

public class ClockTimer {
    @NonNull
    final Clock mClock;

    long mStartTime;

    long mEndTime;

    public ClockTimer(@NonNull Clock clock) {
        mClock = clock;
        start();
    }

    @NonNull
    public Clock getClock() {
        return mClock;
    }

    public void start() {
        mStartTime = mEndTime = mClock.now();
    }

    /**
     * 一定ミリ秒以上経過していたらtrue
     *
     * @param timeMs チェックする時間
     */
    public boolean overTimeMs(long timeMs) {
        long diff = end();
        return diff >= timeMs;
    }

    public long end() {
        mEndTime = mClock.now();
        return (mEndTime - mStartTime);
    }

    /**
     * ストップウォッチを停止し、時間を秒単位で取得する
     */
    public double endSec() {
        return Timer.msToSec(end());
    }

    /**
     * ストップウォッチを停止し、時間を分単位で取得する
     */
    public double endMinute() {
        return Timer.msToMinute(end());
    }

    /**
     * ストップウォッチを停止し、時間をHour単位で取得する
     */
    public double endHour() {
        return Timer.msToHour(end());
    }

    /**
     * ストップウォッチを停止し、時間を日単位で取得する
     */
    public double endDay() {
        return Timer.msToDay(end());
    }

}
