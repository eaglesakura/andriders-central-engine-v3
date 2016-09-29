package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.data.db.CentralSettingDatabase;
import com.eaglesakura.android.garnet.Provider;

import android.content.Context;

/**
 * Manager系の依存管理を行う
 */
public class AppManagerProvider implements Provider {
    static CentralSettingDatabase sCentralSettingDatabase;

    private synchronized static CentralSettingDatabase getCentralSettingDatabase(Context context) {
        if (sCentralSettingDatabase == null) {
            sCentralSettingDatabase = new CentralSettingDatabase(context.getApplicationContext());
        }
        return sCentralSettingDatabase;
    }

    @Override
    public void onDependsCompleted(Object inject) {

    }


    @Override
    public void onInjectCompleted(Object inject) {

    }
}
