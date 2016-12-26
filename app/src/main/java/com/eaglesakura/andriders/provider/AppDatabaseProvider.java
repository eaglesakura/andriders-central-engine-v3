package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.data.db.CentralSettingDatabase;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.android.framework.provider.ContextProvider;
import com.eaglesakura.android.garnet.Provide;

import android.content.Context;

/**
 * DBの依存管理を行う
 */
public class AppDatabaseProvider extends ContextProvider {
    static CentralSettingDatabase sCentralSettingDatabase;

    static SessionLogDatabase sSessionLogDatabaseWrite;

    static SessionLogDatabase sSessionLogDatabaseRead;

    synchronized static CentralSettingDatabase getCentralSettingDatabase(Context context) {
        if (sCentralSettingDatabase == null) {
            sCentralSettingDatabase = new CentralSettingDatabase(context);
        }
        return sCentralSettingDatabase;
    }

    synchronized static SessionLogDatabase getSessionLogDatabaseWritable(Context context) {
        if (sSessionLogDatabaseWrite == null) {
            sSessionLogDatabaseWrite = new SessionLogDatabase(context);
        }
        return sSessionLogDatabaseWrite;
    }

    synchronized static SessionLogDatabase getSessionLogDatabaseReadable(Context context) {
        if (sSessionLogDatabaseRead == null) {
            sSessionLogDatabaseRead = new SessionLogDatabase(context);
        }
        return sSessionLogDatabaseRead;
    }

    /**
     * 読み込みのみのモードで開く
     */
    public static final String NAME_READ_ONLY = "NAME_READ_ONLY";

    /**
     * 書き込み可能モードで取得
     */
    public static final String NAME_WRITEABLE = "NAME_WRITEABLE";

    @Override
    public void onDependsCompleted(Object inject) {

    }

    /**
     * 書き込み可能な状態でDBを取得する（デフォルト挙動）
     */
    @Provide
    public CentralSettingDatabase provideCentralSettingDatabase() {
        return getCentralSettingDatabase(getApplication());
    }

    @Provide(name = NAME_WRITEABLE)
    public SessionLogDatabase proviSessionLogDatabaseWritable() {
        return getSessionLogDatabaseWritable(getApplication());
    }

    @Provide(name = NAME_READ_ONLY)
    public SessionLogDatabase proviSessionLogDatabaseReadable() {
        return getSessionLogDatabaseReadable(getApplication());
    }

    @Override
    public void onInjectCompleted(Object inject) {

    }
}
