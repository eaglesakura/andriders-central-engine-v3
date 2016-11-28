package com.eaglesakura.andriders.system.manager;

import com.eaglesakura.andriders.data.db.CentralSettingDatabase;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.provider.AppDatabaseProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Initializer;
import com.eaglesakura.android.garnet.Inject;

import org.greenrobot.greendao.annotation.NotNull;

import android.content.Context;

/**
 * DBを経由したデータ管理を行う
 */
public abstract class CentralSettingManager {
    final protected Context mContext;

    @Inject(value = AppDatabaseProvider.class)
    CentralSettingDatabase mCentralDatabase;

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    public CentralSettingManager(@NotNull Context context) {
        mContext = context;
    }

    @Initializer
    public void initialize() {
        Garnet.create(this)
                .depend(Context.class, mContext)
                .inject();
    }

    /**
     * DBを書き込み可能な状態で開く
     */
    protected CentralSettingDatabase open() {
        return mCentralDatabase.openWritable(CentralSettingDatabase.class);
    }
}
