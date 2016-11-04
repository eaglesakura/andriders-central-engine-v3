package com.eaglesakura.andriders.central.data.log;

import com.android.annotations.NonNull;
import com.eaglesakura.andriders.serialize.RawCentralData;

import java.util.Date;

/**
 * 一定範囲内のログ統計を扱う
 */
public class LogStatistics {
    @NonNull
    Date mStartDate;

    @NonNull
    Date mEndDate;

    int mActiveTimeMs;

    float mActiveDistanceKm;

    float mSumAltitudeMeter;

    float mSumDistanceKm;

    float mCalories;

    float mExercise;

    float mMaxCadence;

    short mMaxHeartrate;

    float mMaxSpeedKmh;

    public LogStatistics(long startDate) {
        mStartDate = new Date(startDate);
        mEndDate = new Date(startDate);
    }

    public LogStatistics(Date startDate, Date endDate, int activeTimeMs, float activeDistanceKm, float sumAltitudeMeter, float sumDistanceKm, float calories, float exercise, float maxCadence, short maxHeartrate, float maxSpeedKmh) {
        mStartDate = startDate;
        mEndDate = endDate;
        mActiveTimeMs = activeTimeMs;
        mActiveDistanceKm = activeDistanceKm;
        mSumAltitudeMeter = sumAltitudeMeter;
        mSumDistanceKm = sumDistanceKm;
        mCalories = calories;
        mExercise = exercise;
        mMaxCadence = maxCadence;
        mMaxHeartrate = maxHeartrate;
        mMaxSpeedKmh = maxSpeedKmh;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public int getActiveTimeMs() {
        return mActiveTimeMs;
    }

    public float getActiveDistanceKm() {
        return mActiveDistanceKm;
    }

    public float getSumAltitudeMeter() {
        return mSumAltitudeMeter;
    }

    public float getSumDistanceKm() {
        return mSumDistanceKm;
    }

    public float getCalories() {
        return mCalories;
    }

    public float getExercise() {
        return mExercise;
    }

    public float getMaxCadence() {
        return mMaxCadence;
    }

    public float getMaxHeartrate() {
        return mMaxHeartrate;
    }

    public float getMaxSpeedKmh() {
        return mMaxSpeedKmh;
    }

    /**
     * データを更新する
     *
     * @param latest 最新データ
     */
    public void update(@NonNull RawCentralData latest) {
        if (mStartDate == null) {
            mStartDate = new Date(latest.session.startTime);
        }
        mEndDate = new Date(latest.centralStatus.date);
        mActiveTimeMs = latest.session.activeTimeMs;
        mActiveDistanceKm = latest.session.activeDistanceKm;
        mSumAltitudeMeter = latest.session.sumAltitudeMeter;
        mSumDistanceKm = latest.session.distanceKm;
        mCalories = latest.session.fitness.calorie;
        mExercise = latest.session.fitness.exercise;
        if (latest.sensor.cadence != null) {
            mMaxCadence = Math.max(mMaxCadence, latest.sensor.cadence.rpm);
        }

        if (latest.sensor.heartrate != null) {
            mMaxHeartrate = (short) Math.max(mMaxHeartrate, latest.sensor.heartrate.bpm);
        }

        if (latest.sensor.speed != null) {
            mMaxSpeedKmh = Math.max(mMaxSpeedKmh, latest.sensor.speed.speedKmPerHour);
        }
    }
}