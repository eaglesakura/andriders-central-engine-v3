package com.eaglesakura.andriders.db.session;

import com.eaglesakura.andriders.dao.session.DbSessionLog;

import org.greenrobot.greendao.query.CloseableListIterator;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * 各セッションの合計情報を管理する
 */
public class SessionTotal {
    private final DbSessionLog mLog = new DbSessionLog();

    /**
     * 統合されたセッション数
     */
    private final int mSessionNum;

    SessionTotal(@NonNull CloseableListIterator<DbSessionLog> sessions) {
        long startTime = 0;
        long endTime = 0;
        int sessionNum = 0;

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

            ++sessionNum;
        }
        mLog.setStartTime(new Date(startTime));
        mLog.setEndTime(new Date(endTime));
        mSessionNum = sessionNum;
    }

    /**
     * 今日のセッション数を取得する
     */
    public int getSessionNum() {
        return mSessionNum;
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
