package com.eaglesakura.andriders.db.session;

import com.eaglesakura.andriders.dao.session.DaoMaster;
import com.eaglesakura.andriders.dao.session.DaoSession;
import com.eaglesakura.andriders.dao.session.DbSessionLog;
import com.eaglesakura.andriders.dao.session.DbSessionLogDao;
import com.eaglesakura.andriders.dao.session.DbSessionPoint;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.db.DaoDatabase;
import com.eaglesakura.util.DateUtil;
import com.eaglesakura.util.IOUtil;
import com.eaglesakura.util.Timer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import de.greenrobot.dao.query.CloseableListIterator;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * セッションごとのログを保持する
 */
public class SessionLogDatabase extends DaoDatabase<DaoSession> {
    private static final int SUPPORTED_DATABASE_VERSION = 1;

    @NonNull
    final File mDatabasePath;

    /**
     * @param databasePath 保存するDBファイル名 / nullの場合はOnMemoryとして生成される
     * @throws IOException パスが生成できなかった場合に投げられる
     */
    public SessionLogDatabase(@NonNull Context context, @Nullable File databasePath) {
        super(context, DaoMaster.class);
        mDatabasePath = databasePath;

        if (mDatabasePath != null && !IOUtil.mkdirs(mDatabasePath.getParentFile()).isDirectory()) {
            throw new IllegalStateException("Path Error :: " + mDatabasePath.getAbsolutePath());
        }

        if (mDatabasePath != null) {
            AppLog.db("SessionLogDatabase path[%s]", databasePath.getAbsolutePath());
        }
    }

    /**
     * 今日の合計値を読み込む
     */
    public SessionTotal loadTodayTotal(Clock clock) {
        long start = DateUtil.getDateStart(new Date(clock.now()), TimeZone.getDefault()).getTime();
        long end = start + (DateUtil.DAY_MILLI_SEC) - 1;
        return loadTotal(start, end);
    }

    /**
     * 全記録の中から最高速度を取得する
     *
     * @return 最高速度[km/h]
     */
    public double loadMaxSpeedKmh() {
        Timer timer = new Timer();
        try {
            QueryBuilder<DbSessionLog> builder = session.getDbSessionLogDao().queryBuilder();
            CloseableListIterator<DbSessionLog> iterator = builder
                    .orderDesc(DbSessionLogDao.Properties.MaxSpeedKmh)
                    .limit(1)
                    .listIterator();

            if (iterator.hasNext()) {
                return iterator.next().getMaxSpeedKmh();
            } else {
                return 0;
            }
        } finally {
            AppLog.db("loadMaxSpeedKmh readTime[%d ms]", timer.end());
        }
    }

    /**
     * 指定範囲内から最高速度を取得する
     *
     * @param startTime 開始時刻
     * @param endTime   終了時刻
     * @return 最高速度[km/h]
     */
    public double loadMaxSpeedKmh(long startTime, long endTime) {
        Timer timer = new Timer();
        try {
            QueryBuilder<DbSessionLog> builder = session.getDbSessionLogDao().queryBuilder();
            CloseableListIterator<DbSessionLog> iterator = builder
                    .orderDesc(DbSessionLogDao.Properties.MaxSpeedKmh)
                    .where(DbSessionLogDao.Properties.StartTime.ge(startTime), DbSessionLogDao.Properties.EndTime.le(endTime))
                    .limit(1)
                    .listIterator();

            if (iterator.hasNext()) {
                return iterator.next().getMaxSpeedKmh();
            } else {
                return 0;
            }
        } finally {
            AppLog.db("loadMaxSpeedKmh:range readTime[%d ms]", timer.end());
        }
    }

    /**
     * startTime～endTimeまでに開始されたセッションの統計情報を返却する
     *
     * @param startTime 開始時刻
     * @param endTime   終了時刻
     * @return 合計値 / セッションが存在しない場合はnullを返却
     */
    @Nullable
    public SessionTotal loadTotal(long startTime, long endTime) {
        Timer timer = new Timer();
        try {
            QueryBuilder<DbSessionLog> builder = session.getDbSessionLogDao().queryBuilder();

            AppLog.db("loadTotal start(%s) end(%s)", new Date(startTime).toString(), new Date(endTime).toString());

            CloseableListIterator<DbSessionLog> iterator = builder
                    .where(DbSessionLogDao.Properties.StartTime.ge(startTime), DbSessionLogDao.Properties.StartTime.le(endTime))
                    .orderAsc(DbSessionLogDao.Properties.StartTime)
                    .listIterator();
            if (iterator.hasNext()) {
                return new SessionTotal(iterator);
            } else {
                return null;
            }
        } finally {
            AppLog.db("loadTotal readTime[%d ms]", timer.end());
        }
    }

    /**
     * セッション情報を更新する
     *
     * @param currentSession 外部で更新済みのセッション情報
     * @param points         打刻地点一覧
     */
    public void update(DbSessionLog currentSession, Collection<DbSessionPoint> points) {
        Timer timer = new Timer();
        try {
            runInTx(() -> {
                session.insertOrReplace(currentSession);
                for (DbSessionPoint pt : points) {
                    session.insertOrReplace(pt);
                }
                return this;
            });
        } finally {
            AppLog.db("update writeTime[%d ms]", timer.end());
        }
    }

    @Override
    protected SQLiteOpenHelper createHelper() {
        String path = mDatabasePath != null ? mDatabasePath.getAbsolutePath() : null;
        return new SQLiteOpenHelper(context, path, null, SUPPORTED_DATABASE_VERSION) {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                DaoMaster.createAllTables(db, false);
            }
        };
    }
}
