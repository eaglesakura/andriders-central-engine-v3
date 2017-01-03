package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.central.data.log.SessionHeaderCollection;
import com.eaglesakura.andriders.data.backup.CentralBackupExporter;
import com.eaglesakura.andriders.data.backup.serialize.SessionBackup;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.error.io.AppDataNotFoundException;
import com.eaglesakura.andriders.error.io.AppDatabaseException;
import com.eaglesakura.andriders.provider.AppDatabaseProvider;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Initializer;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.lambda.Action1;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.DateUtil;
import com.eaglesakura.util.Timer;

import org.greenrobot.greendao.annotation.NotNull;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
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

    public DataCollection<RawCentralData> listSessionPoints(long sessionId, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        try (SessionLogDatabase db = openReadOnly()) {
            return new DataCollection<>(db.listCentralData(sessionId, cancelCallback));
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

    /**
     * 指定した日のセッションを列挙する
     *
     * @param action 実行内容
     * @return チェックされたポイント数
     */
    public int eachDailySessionPoints(long now, Action1<RawCentralData> action, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        Date dateStart = DateUtil.getDateStart(new Date(now), mTimeZone);
        long dateEnd = dateStart.getTime() + Timer.toMilliSec(1, 0, 0, 0, 0) - 1;
        try (SessionLogDatabase db = openReadOnly()) {
            return db.runInTx(() -> db.eachSessionPoints(dateStart.getTime(), dateEnd, action, cancelCallback));
        } catch (Throwable e) {
            AppException.throwAppExceptionOrTaskCanceled(e);
            return 0;
        }
    }

    public interface ExportCallback {
        /**
         * セッションの書込みを開始する
         */
        void onStart(CentralLogManager self, @NonNull SessionHeader header);

        /**
         * バックアップ情報の書込みを開始する
         */
        void onStartCompless(CentralLogManager self, @NonNull SessionHeader session, SessionBackup backup);
    }

    /**
     * 指定したセッションを含む1日をエクスポートする
     *
     * @param now            セッションを含む日
     * @param dstFile        書込み先ファイル
     * @param cancelCallback キャンセルチェック
     */
    public void exportDailySessions(long now, Uri dstFile, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        try (SessionLogDatabase batch = openReadOnly()) {

            // この日を含んだセッションを列挙する
            SessionHeaderCollection dailySessions = listDailyHeaders(now, cancelCallback);
            if (dailySessions.isEmpty()) {
                throw new AppDataNotFoundException("Date Error");
            }

            exportSessions(dailySessions.list(), dstFile, cancelCallback);
        }
    }

    void exportSessions(List<SessionHeader> headers, Uri dstFile, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        try (SessionLogDatabase batch = openReadOnly()) {
            CentralBackupExporter exporter = new CentralBackupExporter(mContext);
            exporter.export(new CentralBackupExporter.Callback() {
                Iterator<SessionHeader> mIterator = headers.iterator();

                @Nullable
                @Override
                public SessionBackup nextSession(CentralBackupExporter self, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
                    if (!mIterator.hasNext()) {
                        return null;
                    }

                    SessionHeader header = mIterator.next();
                    List<RawCentralData> centralDataList = batch.listCentralData(header.getSessionId(), cancelCallback);
                    if (centralDataList.isEmpty()) {
                        throw new AppDatabaseException("Session Data Error");
                    }

                    return SessionBackup.newInstance(centralDataList);
                }

            }, dstFile, cancelCallback);
        }
    }
}
