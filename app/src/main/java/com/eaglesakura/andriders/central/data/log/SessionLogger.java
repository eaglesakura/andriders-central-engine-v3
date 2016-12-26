package com.eaglesakura.andriders.central.data.log;

import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.sensor.SpeedZone;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.util.Timer;

import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

/**
 * 1セッションのログを管理する
 */
public class SessionLogger {

    /**
     * セッションのトータル情報
     */
    final SessionInfo mSessionInfo;

    /**
     * インメモリキャッシュされたCentral情報
     */
    final List<RawCentralData> mPoints = new LinkedList<>();

    /**
     * 最終保存時刻からの差分時間を記録するタイマー
     */
    @NonNull
    final ClockTimer mPointTimer;

    final Object lock = new Object();

    /**
     * 挿入したログの個数
     */
    int mInsertLogCount = 0;

    /**
     * どの程度の間隔でコミットするか
     */
    static final int POINT_COMMIT_INTERVAL_MS = 1000 * 5;

    public SessionLogger(@NonNull SessionInfo info) {
        mSessionInfo = info;
        mPointTimer = new ClockTimer(info.getSessionClock());
    }

    /**
     * 打刻情報のキャッシュを持っていればtrue
     */
    public boolean hasPointCaches() {
        synchronized (lock) {
            return !mPoints.isEmpty();
        }
    }

    /**
     * キャッシュ数を取得する
     */
    public int getPointCacheCount() {
        synchronized (lock) {
            return mPoints.size();
        }
    }

    /**
     * 打刻を行うデータであればtrue
     */
    private boolean isKeyPoint(RawCentralData data) {
        if (mInsertLogCount == 0) {
            // 初回データは認める
            return true;
        }

        // 速度があり、停止以外のステータスで、かつ速度が最高地点にあるならばコミット
        if (data.sensor.speed != null
                && data.sensor.speed.zone != SpeedZone.Stop
                && data.sensor.speed.speedKmh == data.record.maxSpeedKmhSession) {
            // 速度が最高点に達している場合は強制的にコミットする
            return true;
        }

        // 規定時間を過ぎたので現時点を打刻する
        if (mPointTimer.overTimeMs(POINT_COMMIT_INTERVAL_MS)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 強制的に打刻を行う
     */
    public void add(RawCentralData latest) {
        synchronized (lock) {
            ++mInsertLogCount;
            mPoints.add(latest);
        }
    }

    /**
     * 毎時更新を行う
     *
     * 必要に応じてキーが打刻される
     */
    public void onUpdate(RawCentralData latest) {
        if (isKeyPoint(latest)) {
            mPointTimer.start();
            add(latest);
        }
    }

    /**
     * 書き込みを行う。
     * トランザクションは外部で管理する。
     *
     * これはimport等、特殊な環境下でのトランザクションを外部で調整できるようにするためである。
     *
     * @param writableDb 書き込みモードでOpenされているDB
     */
    public void commit(SessionLogDatabase writableDb) {
        List<RawCentralData> points;
        synchronized (lock) {
            if (mPoints.isEmpty()) {
                return;
            }

            if (mPointTimer.getClock().absDiff(mSessionInfo.getSessionId()) < (1000 * 30)) {
                // 規定時間に満たない場合は保存しない
                // BLEの接続不良で何度もセッションを立ち上げる可能性があるため、それを考慮する
                AppLog.db("Session Write Canceled / cache.num[%d]", mPoints.size());
                return;
            }

            // ローカルにコピーし、メンバ変数はすぐに開放してしまう
            points = new LinkedList<>(mPoints);
            mPoints.clear();
        }

        // DBを書き込む
        Timer timer = new Timer();
        try {
            writableDb.insert(points);
            AppLog.db("Session Log Commit :: time[%s ms] num[%d]", timer.end(), points.size());

        } catch (Exception e) {
            AppLog.db("SessionLog write failed. rollback");
            AppLog.report(e);
            // 書き込みに失敗したのでロールバックする
            synchronized (lock) {
                try {
                    mPoints.addAll(points);
                } catch (Exception e2) {
                    AppLog.db("SessionLog write failed. rollback failed.");
                }
            }
        }
    }
}
