package com.eaglesakura.andriders.central.log;

import com.eaglesakura.andriders.central.session.SessionInfo;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
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

    @NonNull
    final ClockTimer mPointTimer;

    final Object lock = new Object();

    /**
     * コミット成功回数
     */
    int mCommitCount;

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
     * 毎時更新を行う
     */
    public void onUpdate(RawCentralData latest) {
        synchronized (lock) {
            // 規定時間を過ぎたので現時点を打刻する
            if (mPointTimer.overTimeMs(POINT_COMMIT_INTERVAL_MS)) {
                mPoints.add(latest);
                mPointTimer.start();
            }
        }
    }

    /**
     * データをDBに書き込む
     */
    public void commit() {
        List<RawCentralData> points;
        synchronized (lock) {
            if (mPoints.isEmpty()) {
                return;
            }

            if (mPointTimer.getClock().absDiff(mSessionInfo.getSessionId()) < (1000 * 30)) {
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
        Timer timer = new Timer();
        try (SessionLogDatabase db = new SessionLogDatabase(mSessionInfo.getContext()).openWritable(SessionLogDatabase.class)) {
            db.runInTx(() -> {
                if (mCommitCount == 0) {
                    // 初回コミットのみ、インフォメーションを書き込む
                }

                db.insert(points);
                return 0;
            });
            AppLog.db("Session Log Commit :: time[%s] pt[%d]", timer.end(), points.size());
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
