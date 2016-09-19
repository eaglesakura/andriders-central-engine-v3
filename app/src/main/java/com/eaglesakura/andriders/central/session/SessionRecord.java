package com.eaglesakura.andriders.central.session;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.eaglesakura.andriders.central.log.LogStatistics;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawRecord;
import com.eaglesakura.andriders.serialize.RawSessionData;

/**
 * セッション内で管理すべき統計情報を扱う。
 *
 * セッションでは「今日」「現在セッション」の2統計を同時に扱う必要がある。
 * 本来はDBにログ統計を行わせるべきであるが、リアルタイムや消費電力的に無理があるので、Runtimeで計算する。
 * 同様の計算をログ画面でも行うため、結果は一致するはずである。
 */
public class SessionRecord {
    /**
     * 全セッションの統計
     */
    LogStatistics mTotalStatistics;

    /**
     * 今日一日の統計
     */
    LogStatistics mTodayStatistics;

    /**
     * 現セッションでの統計
     */
    LogStatistics mSessionStatistics;

    public SessionRecord(@NonNull SessionInfo info, @Nullable LogStatistics totalStatistics, @Nullable LogStatistics todayStatistics) {
        mTotalStatistics = totalStatistics;
        mTodayStatistics = todayStatistics;
        mSessionStatistics = new LogStatistics(info.getSessionClock().now());
    }

    /**
     * 今日のログを取得する
     */
    public void getTotalData(RawCentralData.RawCentralStatus central, RawSessionData dstTodayLog, RawRecord dstRecord) {
        synchronized (this) {
            // セッションから最高記録を生成する
            dstTodayLog.flags = 0x00;
            dstTodayLog.sessionId = null;   // 今日の統計なので、セッションIDは存在しない
            dstTodayLog.activeTimeMs = mSessionStatistics.getActiveTimeMs();
            dstTodayLog.activeDistanceKm = mSessionStatistics.getActiveDistanceKm();
            dstTodayLog.distanceKm = mSessionStatistics.getSumDistanceKm();
            dstTodayLog.startTime = mSessionStatistics.getStartDate().getTime();
            dstTodayLog.sumAltitudeMeter = mSessionStatistics.getSumAltitudeMeter();
            dstTodayLog.fitness = new RawSessionData.RawFitnessStatus();
            dstTodayLog.fitness.calorie = mSessionStatistics.getCalories();
            dstTodayLog.fitness.exercise = mSessionStatistics.getExercise();
            dstTodayLog.fitness.mets = 0;

            dstRecord.maxHeartrateSession = (short) mSessionStatistics.getMaxHeartrate();
            dstRecord.maxHeartrateToday = dstRecord.maxHeartrateSession;

            dstRecord.maxSpeedKmhSession = mSessionStatistics.getMaxSpeedKmh(); // セッション最高記録
            dstRecord.maxSpeedKmhToday = mSessionStatistics.getMaxSpeedKmh();   // 今日最高記録

            // 今日の既存ログを統計に含める
            if (mTodayStatistics != null) {
                dstTodayLog.startTime = mTodayStatistics.getStartDate().getTime();

                dstTodayLog.activeTimeMs += mTodayStatistics.getActiveTimeMs();
                dstTodayLog.activeDistanceKm += mTodayStatistics.getActiveDistanceKm();
                dstTodayLog.distanceKm += mTodayStatistics.getSumDistanceKm();
                dstTodayLog.sumAltitudeMeter += mTodayStatistics.getSumAltitudeMeter();
                dstTodayLog.fitness.calorie += mTodayStatistics.getCalories();
                dstTodayLog.fitness.exercise += mTodayStatistics.getExercise();

                dstRecord.maxSpeedKmhToday = Math.max(dstRecord.maxSpeedKmhSession, mTodayStatistics.getMaxSpeedKmh());
                dstRecord.maxHeartrateToday = (short) Math.max(dstRecord.maxHeartrateSession, mTodayStatistics.getMaxHeartrate());
            }

            // 全ログを統計に含める
            if (mTotalStatistics != null) {
                dstRecord.maxSpeedKmh = Math.max(dstRecord.maxSpeedKmh, mTotalStatistics.getMaxSpeedKmh());
            }

            // 最終的な値を適用する
            dstRecord.maxSpeedKmh = Math.max(dstRecord.maxSpeedKmhToday, mSessionStatistics.getMaxSpeedKmh());  // 全ての過去を含めた最高記録
            dstTodayLog.durationTimeMs = (int) (central.date - dstTodayLog.startTime);
        }
    }

    public void update(RawCentralData latest) {
        // セッション情報を更新する
        mSessionStatistics.update(latest);
    }
}
