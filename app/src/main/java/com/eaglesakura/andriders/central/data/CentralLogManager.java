package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.central.data.log.SessionHeaderCollection;
import com.eaglesakura.andriders.data.backup.CentralBackupExporter;
import com.eaglesakura.andriders.data.backup.CentralBackupImporter;
import com.eaglesakura.andriders.data.backup.serialize.BackupInformation;
import com.eaglesakura.andriders.data.backup.serialize.SessionBackup;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.error.io.AppDataNotFoundException;
import com.eaglesakura.andriders.error.io.AppDatabaseException;
import com.eaglesakura.andriders.provider.AppDatabaseProvider;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.db.DaoDatabase;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Initializer;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.cerberus.error.TaskCanceledException;
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

import java.util.ArrayList;
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
        return mSessionLogDatabaseRead.open(DaoDatabase.FLAG_READ_ONLY);
    }

    SessionLogDatabase openWrite() {
        return mSessionLogDatabaseRead.open(0x00);
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

    /**
     * バックアップ書き出し時のコールバック
     */
    public interface ExportCallback {
        /**
         * セッションの書込みを開始する
         */
        void onStart(CentralLogManager self, @NonNull SessionHeader header);

        /**
         * バックアップ情報の書込みを開始する
         */
        void onStartCompress(CentralLogManager self, @NonNull SessionHeader session, SessionBackup backup);
    }

    /**
     * バックアップ読み込み時のコールバック
     */
    public interface ImportCallback {
        void onInsertStart(CentralLogManager self, @NonNull SessionBackup backup);
    }

    /**
     * 指定したセッションを含む1日をエクスポートする
     *
     * @param now            セッションを含む日
     * @param dstFile        書込み先ファイル
     * @param cancelCallback キャンセルチェック
     */
    public DataCollection<SessionHeader> exportDailySessions(long now, ExportCallback exportCallback, Uri dstFile, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        try (SessionLogDatabase batch = openReadOnly()) {

            // この日を含んだセッションを列挙する
            SessionHeaderCollection dailySessions = listDailyHeaders(now, cancelCallback);
            if (dailySessions.isEmpty()) {
                throw new AppDataNotFoundException("Date Error");
            }

            return exportSessions(dailySessions.list(), exportCallback, dstFile, cancelCallback);
        }
    }

    DataCollection<SessionHeader> exportSessions(List<SessionHeader> headers, ExportCallback exportCallback, Uri dstFile, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        List<SessionHeader> result = new ArrayList<>();
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
                    result.add(header);
                    exportCallback.onStart(CentralLogManager.this, header);
                    List<RawCentralData> centralDataList = batch.listCentralData(header.getSessionId(), cancelCallback);
                    if (centralDataList.isEmpty()) {
                        throw new AppDatabaseException("Session Data Error");
                    }

                    SessionBackup sessionBackup = SessionBackup.newInstance(centralDataList);
                    exportCallback.onStartCompress(CentralLogManager.this, header, sessionBackup);
                    return sessionBackup;
                }

            }, dstFile, cancelCallback);
        }

        return new DataCollection<>(result);
    }

    /**
     * @param callback       インポート経過コールバック
     * @param backupFile     バックアップされたファイル
     * @param cancelCallback キャンセルチェック
     */
    public DataCollection<SessionHeader> importFromBackup(ImportCallback callback, Uri backupFile, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        CentralBackupImporter importer = new CentralBackupImporter(mContext);
        try (SessionLogDatabase db = openWrite()) {
            return db.runInTx(() -> {
                List<SessionHeader> result = new ArrayList<>();

                importer.parse(new CentralBackupImporter.Callback() {
                    @Override
                    public void onLoadInformation(@NonNull CentralBackupImporter self, @NonNull BackupInformation info, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
                        AppLog.db("Backup Info");
                        AppLog.db("  - AppInfo app[%s] schema[%d]", info.appVersionName, info.version);
                        AppLog.db("  - Date[%s]", new Date(info.exportDate));
                        AppLog.db("  - Device[%s]", info.deviceName);
                    }

                    @Override
                    public void onLoadSession(@NonNull CentralBackupImporter self, @NonNull SessionBackup session, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
                        callback.onInsertStart(CentralLogManager.this, session);
                        db.insert(session.points, cancelCallback);
                        SessionHeader header = new SessionHeader(session.points.get(session.points.size() - 1));
                        result.add(header);
                    }
                }, backupFile, cancelCallback);

                return new DataCollection<>(result);
            });
        } catch (Throwable e) {
            AppException.throwAppException(e);
            throw new Error();
        }
    }
}
