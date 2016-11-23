package com.eaglesakura.andriders.central.data.log;

import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.provider.AppDatabaseProvider;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
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

    /**
     * Database
     */
    @Inject(AppDatabaseProvider.class)
    SessionLogDatabase mDatabase;

    final Object lock = new Object();

    /**
     * どの程度の間隔でコミットするか
     */
    static final int POINT_COMMIT_INTERVAL_MS = 1000 * 5;

    public SessionLogger(@NonNull SessionInfo info) {
        this(info, Garnet.instance(AppDatabaseProvider.class, SessionLogDatabase.class));
    }

    public SessionLogger(@NonNull SessionInfo info, SessionLogDatabase database) {
        mSessionInfo = info;
        mPointTimer = new ClockTimer(info.getSessionClock());
        mDatabase = database;

        if (mDatabase == null) {
            throw new NullPointerException("Database == null");
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
            mPoints.add(latest);
        }
    }

    /**
     * 毎時更新を行う
     */
    public void onUpdate(RawCentralData latest) {
        synchronized (lock) {
            if (isKeyPoint(latest)) {
                mPointTimer.start();
                mPoints.add(latest);
            }
        }
    }

    /**
     * 管理用データベースクラスを取得する
     */
    @NonNull
    public SessionLogDatabase getDatabase() {
        return mDatabase;
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
                AppLog.db("Session Write Canceled");
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

    /**
     * データをDBに書き込む
     */
    public void commit() {
        try (SessionLogDatabase db = mDatabase.openWritable(SessionLogDatabase.class)) {
            db.runInTx(() -> {
                commit(db);
                return 0;
            });
        }
    }
}
