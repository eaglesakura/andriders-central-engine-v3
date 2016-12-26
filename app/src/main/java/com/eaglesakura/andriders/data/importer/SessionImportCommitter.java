package com.eaglesakura.andriders.data.importer;

import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.central.data.log.SessionLogger;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.provider.AppDatabaseProvider;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * セッション情報のインストール管理を行う
 *
 * Import時にキャンセル制御を正しく行うため、トランザクションは外部で行うことを前提とする
 */
public class SessionImportCommitter implements SessionImporter.Listener {
    @NonNull
    final Context mContext;

    @Inject(value = AppDatabaseProvider.class, name = AppDatabaseProvider.NAME_WRITEABLE)
    SessionLogDatabase mDatabase;

    /**
     * 現在のセッションのロガー
     */
    private SessionLogger mSessionLogger;

    /**
     * 現在のセッション管理
     */
    private CentralDataManager mCurrentManager;

    public SessionImportCommitter(@NonNull Context context) {
        mContext = context;

        Garnet.create(this)
                .depend(Context.class, context)
                .inject();
    }

    public SessionLogDatabase openDatabase() {
        return mDatabase.openWritable(SessionLogDatabase.class);
    }

    @Override
    public void onSessionStart(SessionImporter self, CentralDataManager dataManager) throws AppException {
        mCurrentManager = dataManager;
        mSessionLogger = new SessionLogger(mCurrentManager.getSessionInfo());
    }

    @Override
    public void onPointInsert(SessionImporter self, CentralDataManager dataManager, RawCentralData latest) throws AppException {
        mSessionLogger.onUpdate(latest);
        if (mSessionLogger.getPointCacheCount() > 100) {
            commit();
        }
    }

    @Override
    public void onSessionFinished(SessionImporter self, CentralDataManager dataManager) throws AppException {
        mSessionLogger.add(dataManager.getLatestCentralData());
        commit();
    }

    private void commit() {
        if (mSessionLogger.hasPointCaches()) {
            mSessionLogger.commit(mDatabase);
        }
    }
}
