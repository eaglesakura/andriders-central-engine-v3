package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.central.data.log.SessionHeaderCollection;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.provider.AppDatabaseProvider;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Initializer;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.DateUtil;
import com.eaglesakura.util.Timer;

import org.greenrobot.greendao.annotation.NotNull;

import android.content.Context;

import java.util.Date;
import java.util.TimeZone;

/**
 * ログを管理する
 */
public class CentralLogManager {

    Context mContext;

    @Inject(value = AppDatabaseProvider.class, name = AppDatabaseProvider.NAME_READ_ONLY)
    SessionLogDatabase mSessionLogDatabaseRead;

    TimeZone mTimeZone = TimeZone.getDefault();

    public CentralLogManager(@NotNull Context context) {
        mContext = context;
    }

    @Initializer
    public void initialize() {
        Garnet.create(this)
                .depend(Context.class, mContext)
                .inject();
    }

    SessionLogDatabase openReadOnly() {
        return mSessionLogDatabaseRead.openReadOnly(SessionLogDatabase.class);
    }

    SessionLogDatabase openWrite() {
        return mSessionLogDatabaseRead.openReadOnly(SessionLogDatabase.class);
    }

    /**
     * セッションを削除する
     *
     * @param session 削除対象セッション
     */
    public void delete(SessionHeader session) throws AppException {
        try (SessionLogDatabase db = openWrite()) {
            db.runInTx(() -> {
                db.deleteSession(session.getSessionId());
                return 0;
            });
        }
    }

    /**
     * 全てのセッション情報を返却する
     */
    public SessionHeaderCollection listAllHeaders(CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        try (SessionLogDatabase db = openReadOnly()) {
            return new SessionHeaderCollection(db.loadHeaders(0, 0, cancelCallback));
        }
    }

    /**
     * 指定した日のセッション情報をロードする
     */
    public SessionHeaderCollection listDailyHeaders(long now, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        try (SessionLogDatabase db = openReadOnly()) {
            Date dateStart = DateUtil.getDateStart(new Date(now), mTimeZone);
            return new SessionHeaderCollection(
                    db.loadHeaders(dateStart.getTime(), dateStart.getTime() + Timer.toMilliSec(1, 0, 0, 0, 0) - 1, cancelCallback)
            );
        }
    }

    /**
     * セッション統計情報をロードする
     *
     * @param header         ヘッダ
     * @param cancelCallback キャンセルチェック
     */
    public LogStatistics loadSessionStatistics(SessionHeader header, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        try (SessionLogDatabase db = openReadOnly()) {
            return db.loadTotal(header.getSessionId() - 1, header.getEndDate().getTime() + 1, cancelCallback);
        }
    }

    /**
     * 指定した日の記録を生成する
     */
    public LogStatistics loadDailyStatistics(long now, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        try (SessionLogDatabase db = openReadOnly()) {
            Date dateStart = DateUtil.getDateStart(new Date(now), mTimeZone);
            return db.loadTotal(dateStart.getTime(), dateStart.getTime() + Timer.toMilliSec(1, 0, 0, 0, 0) - 1, cancelCallback);
        }
    }

    /**
     * 全ての期間の記録を生成する
     */
    public LogStatistics loadAllStatistics(CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        try (SessionLogDatabase db = openReadOnly()) {
            return db.loadTotal(0, 0, cancelCallback);
        }
    }
}
