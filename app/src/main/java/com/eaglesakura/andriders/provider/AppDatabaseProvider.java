package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.data.db.CentralSettingDatabase;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.android.garnet.Provide;

import android.content.Context;

/**
 * DBの依存管理を行う
 */
public class AppDatabaseProvider extends AppBaseProvider {
    static CentralSettingDatabase sCentralSettingDatabase;

    static SessionLogDatabase sSessionLogDatabase;

    synchronized static CentralSettingDatabase getCentralSettingDatabase(Context context) {
        if (sCentralSettingDatabase == null) {
            sCentralSettingDatabase = new CentralSettingDatabase(context);
        }
        return sCentralSettingDatabase;
    }

    synchronized static SessionLogDatabase getSessionLogDatabase(Context context) {
        if (sSessionLogDatabase == null) {
            sSessionLogDatabase = new SessionLogDatabase(context);
        }
        return sSessionLogDatabase;
    }

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

    @Provide
    public SessionLogDatabase proviSessionLogDatabase() {
        return getSessionLogDatabase(getApplication());
    }

    @Override
    public void onInjectCompleted(Object inject) {

    }
}
