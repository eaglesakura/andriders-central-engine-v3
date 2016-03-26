package com.eaglesakura.andriders.central.log;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.dao.session.DbSessionLog;
import com.eaglesakura.andriders.dao.session.DbSessionPoint;
import com.eaglesakura.andriders.db.session.SessionLogDatabase;
import com.eaglesakura.andriders.db.session.SessionTotal;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSessionData;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.android.util.AndroidThreadUtil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 1セッションのログを管理する
 */
public class SessionLogger {
    /**
     * このセッションを除いた今日の統計情報
     *
     * 今日初のセッションの場合はnullが保持される。
     */
    @NonNull
    final SessionTotal mTodayTotal;

    /**
     * このセッションでの統計情報
     */
    @NonNull
    final DbSessionLog mSessionLog = new DbSessionLog();

    /**
     * インメモリキャッシュされたポイント情報
     */
    final List<DbSessionPoint> mPoints = new LinkedList<>();

    @NonNull
    final ClockTimer mPointTimer;

    @NonNull
    final String mSessionId;

    @NonNull
    final Context mContext;

    @Nullable
    final File mDatabasePath;

    /**
     * どの程度の間隔でコミットするか
     */
    static final int POINT_COMMIT_INTERVAL_MS = 1000 * 15;

    public SessionLogger(@NonNull Context context, @NonNull String sessionId, @Nullable File databasePath, @NonNull Clock clock) {
        mContext = context.getApplicationContext();
        mPointTimer = new ClockTimer(clock);
        mDatabasePath = databasePath;
        mSessionId = sessionId;

        mSessionLog.setSessionId(sessionId);
        mSessionLog.setStartTime(new Date(clock.now()));
        mSessionLog.setEndTime(mSessionLog.getStartTime());

        SessionLogDatabase db = new SessionLogDatabase(context, databasePath);
        try {
            db.openReadOnly();
            mTodayTotal = db.loadTodayTotal();
        } finally {
            db.close();
        }
    }

    /**
     * 今日のログを取得する
     */
    public void getTodayTotal(RawSessionData dst) {
        dst.flags = 0x00;
        dst.activeTimeMs = (int) mSessionLog.getActiveTimeMs();
        dst.activeDistanceKm = (float) mSessionLog.getActiveDistanceKm();
        dst.distanceKm = (float) mSessionLog.getSumDistanceKm();
        dst.sessionId = null;
        dst.startTime = mSessionLog.getStartTime().getTime();
        dst.sumAltitudeMeter = (float) mSessionLog.getSumAltitude();

        dst.fitness = new RawSessionData.RawFitnessStatus();
        dst.fitness.calorie = (float) mSessionLog.getCalories();
        dst.fitness.exercise = (float) mSessionLog.getExercise();
        dst.fitness.mets = 0;

        if (mTodayTotal != null) {
            dst.activeTimeMs += mTodayTotal.getActiveTimeMs();
            dst.activeDistanceKm += (float) mTodayTotal.getActiveDistanceKm();
            dst.distanceKm += (float) mTodayTotal.getSumDistanceKm();
            dst.startTime = mTodayTotal.getStartTime().getTime();
            dst.sumAltitudeMeter += (float) mTodayTotal.getSumAltitude();

            dst.fitness.calorie += (float) mTodayTotal.getCalories();
            dst.fitness.exercise += (float) mTodayTotal.getExercise();
        }
        dst.durationTimeMs = (int) (mPointTimer.getClock().now() - dst.startTime);
    }

    /**
     * 毎時更新を行う
     */
    public void onUpdate(RawCentralData latest) {
        if (latest == null) {
            return;
        }

        // 規定時間を過ぎたので現時点を打刻する
        if (!mPointTimer.overTimeMs(POINT_COMMIT_INTERVAL_MS)) {
            DbSessionPoint pt = new DbSessionPoint();
            pt.setDate(new Date(latest.centralStatus.date));
            pt.setCentral(AceUtils.publicFieldSerialize(latest));
            mPoints.add(pt);

            mPointTimer.start();
        }

        // セッション情報を更新する
        mSessionLog.setEndTime(new Date(mPointTimer.getClock().now()));
        mSessionLog.setActiveTimeMs(latest.session.activeTimeMs);
        mSessionLog.setActiveDistanceKm(latest.session.activeDistanceKm);
        mSessionLog.setSumAltitude(latest.session.sumAltitudeMeter);
        mSessionLog.setSumDistanceKm(latest.session.distanceKm);
        mSessionLog.setCalories(latest.session.fitness.calorie);
        mSessionLog.setExercise(latest.session.fitness.exercise);
        if (latest.sensor.cadence != null) {
            mSessionLog.setMaxCadence(Math.max(mSessionLog.getMaxCadence(), (int) latest.sensor.cadence.rpm));
        }
        if (latest.sensor.heartrate != null) {
            mSessionLog.setMaxHeartrate(Math.max(mSessionLog.getMaxHeartrate(), (int) latest.sensor.heartrate.bpm));
        }
        if (latest.sensor.speed != null) {
            mSessionLog.setMaxSpeedKmh(Math.max(mSessionLog.getMaxSpeedKmh(), (int) latest.sensor.speed.speedKmPerHour));
        }
    }

    /**
     * データをDBに書き込む
     */
    public void commit() {
        AndroidThreadUtil.assertBackgroundThread();

        SessionLogDatabase db = new SessionLogDatabase(mContext, mDatabasePath);
        try {
            db.openWritable();
            db.update(mSessionLog, mPoints);

            // 書き込みが成功したのでインメモリキャッシュを開放する
            mPoints.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
}
