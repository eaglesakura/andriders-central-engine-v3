package com.eaglesakura.andriders.central.log;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.dao.session.DbSessionLog;
import com.eaglesakura.andriders.dao.session.DbSessionPoint;
import com.eaglesakura.andriders.db.session.SessionLogDatabase;
import com.eaglesakura.andriders.db.session.SessionTotal;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSessionData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
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

    final Object lock = new Object();

    /**
     * どの程度の間隔でコミットするか
     */
    static final int POINT_COMMIT_INTERVAL_MS = 1000 * 5;

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
            mTodayTotal = db.loadTodayTotal(clock);
        } finally {
            db.close();
        }
    }

    /**
     * 打刻情報のキャッシュを持っていればtrue
     */
    public boolean hasPointCaches() {
        synchronized (lock) {
            return !mPoints.isEmpty();
        }
    }

    @NonNull
    public SessionTotal getTotalData() {
        return mTodayTotal;
    }

    /**
     * 今日のログを取得する
     */
    public void getTotalData(RawSessionData dst) {
        synchronized (lock) {
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
    }

    /**
     * 毎時更新を行う
     */
    public void onUpdate(RawCentralData latest) {
        synchronized (lock) {
            // 規定時間を過ぎたので現時点を打刻する
            if (mPointTimer.overTimeMs(POINT_COMMIT_INTERVAL_MS)) {
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
    }

    /**
     * データをDBに書き込む
     */
    public void commit() {
        List<DbSessionPoint> points;
        synchronized (lock) {
            if (mPoints.isEmpty()) {
                return;
            }

            if (mPointTimer.getClock().absDiff(mSessionLog.getStartTime().getTime()) < (1000 * 30)) {
                // 規定時間に満たない場合は保存しない
                // BLEの接続不良で何度もセッションを立ち上げる可能性があるため、それを考慮する
                AppLog.db("Session Write Canceled");
                return;
            }

            // ローカルにコピーし、メンバ変数はすぐに開放してしまう
            points = new LinkedList<>(mPoints);
            mPoints.clear();
        }


        // DBを書き込む
        SessionLogDatabase db = new SessionLogDatabase(mContext, mDatabasePath);
        try {
            db.openWritable();
            db.update(mSessionLog, points);
            AppLog.db("Session Log Commit :: time[%s] pt[%d]", mSessionLog.getEndTime(), points.size());
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.db("SessionLog write failed. rollback");
            // 書き込みに失敗したのでロールバックする
            synchronized (lock) {
                try {
                    mPoints.addAll(points);
                } catch (Exception e2) {
                    AppLog.db("SessionLog write failed. rollback failed.");
                }
            }
        } finally {
            db.close();
        }
    }
}
