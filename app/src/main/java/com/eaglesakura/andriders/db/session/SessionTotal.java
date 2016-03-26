package com.eaglesakura.andriders.db.session;

import com.eaglesakura.andriders.dao.session.DbSessionLog;
import com.eaglesakura.util.DateUtil;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.Iterator;

/**
 * 各セッションの合計情報を管理する
 */
public class SessionTotal {
    private final DbSessionLog mLog = new DbSessionLog();

    public SessionTotal(@NonNull Iterator<DbSessionLog> sessions) {

        long startTime = 0;
        long endTime = 0;

        while (sessions.hasNext()) {
            DbSessionLog next = sessions.next();


            if (startTime == 0) {
                startTime = next.getStartTime().getTime();
            } else {
                startTime = Math.min(startTime, next.getStartTime().getTime());
            }
            endTime = Math.max(endTime, next.getEndTime().getTime());

            mLog.setMaxCadence(Math.max(mLog.getMaxCadence(), next.getMaxCadence()));
            mLog.setMaxHeartrate(Math.max(mLog.getMaxHeartrate(), next.getMaxHeartrate()));
            mLog.setMaxSpeedKmh(Math.max(mLog.getMaxSpeedKmh(), next.getMaxSpeedKmh()));
            mLog.setSumAltitude(mLog.getSumAltitude() + next.getSumAltitude());
            mLog.setSumDistanceKm(mLog.getSumDistanceKm() + next.getSumDistanceKm());
            mLog.setCalories(mLog.getCalories() + next.getCalories());
            mLog.setExercise(mLog.getExercise() + next.getExercise());
        }

        mLog.setStartTime(new Date(startTime));
        mLog.setEndTime(new Date(endTime));
    }

    public Date getStartTime() {
        return mLog.getStartTime();
    }

    public Date getEndTime() {
        return mLog.getEndTime();
    }

    public long getActiveTimeMs() {
        return mLog.getActiveTimeMs();
    }

    public double getActiveDistanceKm() {
        return mLog.getActiveDistanceKm();
    }

    public double getMaxSpeedKmh() {
        return mLog.getMaxSpeedKmh();
    }

    public int getMaxCadence() {
        return mLog.getMaxCadence();
    }

    public int getMaxHeartrate() {
        return mLog.getMaxHeartrate();
    }

    public double getSumAltitude() {
        return mLog.getSumAltitude();
    }

    public double getSumDistanceKm() {
        return mLog.getSumDistanceKm();
    }

    public double getCalories() {
        return mLog.getCalories();
    }

    public double getExercise() {
        return mLog.getExercise();
    }
}
