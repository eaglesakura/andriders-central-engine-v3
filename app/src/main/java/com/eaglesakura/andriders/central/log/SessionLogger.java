package com.eaglesakura.andriders.central.log;

import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.andriders.dao.session.DbSessionLog;
import com.eaglesakura.andriders.dao.session.DbSessionPoint;
import com.eaglesakura.andriders.db.session.SessionLogDatabase;
import com.eaglesakura.andriders.db.session.SessionTotal;
import com.eaglesakura.andriders.db.storage.AppStorageManager;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawRecord;
import com.eaglesakura.andriders.serialize.RawSessionData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;

import android.content.Context;
import android.support.annotation.NonNull;

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
     * 全体の最速記録
     */
    final double mTotalFastestSpeedKmh;

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

    final Object lock = new Object();

    /**
     * どの程度の間隔でコミットするか
     */
    static final int POINT_COMMIT_INTERVAL_MS = 1000 * 5;

    public SessionLogger(@NonNull Context context, @NonNull String sessionId, @NonNull AppStorageManager storageManager, @NonNull Clock clock) {
        mContext = context.getApplicationContext();
        mPointTimer = new ClockTimer(clock);
        mSessionId = sessionId;

        mSessionLog.setSessionId(sessionId);
        mSessionLog.setStartTime(new Date(clock.now()));
        mSessionLog.setEndTime(mSessionLog.getStartTime());

        SessionLogDatabase db = new SessionLogDatabase(context);
        try {
            db.openReadOnly();
            mTodayTotal = db.loadTodayTotal(clock);
            mTotalFastestSpeedKmh = db.loadMaxSpeedKmh();
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
    public void getTotalData(RawSessionData dstTodayLog, RawRecord dstRecord) {
        synchronized (lock) {
            {
                dstTodayLog.flags = 0x00;
                dstTodayLog.activeTimeMs = (int) mSessionLog.getActiveTimeMs();
                dstTodayLog.activeDistanceKm = (float) mSessionLog.getActiveDistanceKm();
                dstTodayLog.distanceKm = (float) mSessionLog.getSumDistanceKm();
                dstTodayLog.sessionId = null;
                dstTodayLog.startTime = mSessionLog.getStartTime().getTime();
                dstTodayLog.sumAltitudeMeter = (float) mSessionLog.getSumAltitude();

                dstTodayLog.fitness = new RawSessionData.RawFitnessStatus();
                dstTodayLog.fitness.calorie = (float) mSessionLog.getCalories();
                dstTodayLog.fitness.exercise = (float) mSessionLog.getExercise();
                dstTodayLog.fitness.mets = 0;

                if (mTodayTotal != null) {
                    dstTodayLog.activeTimeMs += mTodayTotal.getActiveTimeMs();
                    dstTodayLog.activeDistanceKm += (float) mTodayTotal.getActiveDistanceKm();
                    dstTodayLog.distanceKm += (float) mTodayTotal.getSumDistanceKm();
                    dstTodayLog.startTime = mTodayTotal.getStartTime().getTime();
                    dstTodayLog.sumAltitudeMeter += (float) mTodayTotal.getSumAltitude();

                    dstTodayLog.fitness.calorie += (float) mTodayTotal.getCalories();
                    dstTodayLog.fitness.exercise += (float) mTodayTotal.getExercise();
                }
                dstTodayLog.durationTimeMs = (int) (mPointTimer.getClock().now() - dstTodayLog.startTime);
            }
            {
                // セッション情報チェック
                dstRecord.maxHeartrateSession = (short) mSessionLog.getMaxHeartrate();
                dstRecord.maxSpeedKmhSession = (float) mSessionLog.getMaxSpeedKmh();

                // 今日の記録を必要に応じて上書きチェックする
                if (mTodayTotal != null) {
                    dstRecord.maxSpeedKmhToday = Math.max(dstRecord.maxSpeedKmhSession, (float) mTodayTotal.getMaxSpeedKmh());
                    dstRecord.maxHeartrateToday = (short) Math.max(dstRecord.maxHeartrateSession, mTodayTotal.getMaxHeartrate());
                } else {
                    // 今日初セッションはセッション情報と同義になる
                    dstRecord.maxHeartrateToday = dstRecord.maxHeartrateSession;
                    dstRecord.maxSpeedKmhToday = dstRecord.maxSpeedKmhSession;
                }

                // 全体最速を求める
                dstRecord.maxSpeedKmh = (float) Math.max(dstRecord.maxSpeedKmhToday, mTotalFastestSpeedKmh);
            }
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
                pt.setCentral(AppUtil.publicFieldSerialize(latest));
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
                mSessionLog.setMaxCadence(Math.max(mSessionLog.getMaxCadence(), latest.sensor.cadence.rpm));
            }
            if (latest.sensor.heartrate != null) {
                mSessionLog.setMaxHeartrate(Math.max(mSessionLog.getMaxHeartrate(), latest.sensor.heartrate.bpm));
            }
            if (latest.sensor.speed != null) {
                mSessionLog.setMaxSpeedKmh(Math.max(mSessionLog.getMaxSpeedKmh(), latest.sensor.speed.speedKmPerHour));
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
        SessionLogDatabase db = new SessionLogDatabase(mContext);
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
