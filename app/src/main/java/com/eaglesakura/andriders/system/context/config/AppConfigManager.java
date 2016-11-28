package com.eaglesakura.andriders.system.context.config;

import com.eaglesakura.andriders.v3.gen.config.AppStatusConfig;
import com.eaglesakura.android.firebase.config.FirebaseReferenceConfigManager;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Firebase同期固定データ
 */
public class AppConfigManager extends FirebaseReferenceConfigManager<FbConfigRoot> {
    static final int SCHEMA_VERSION = 1;

    public AppConfigManager(@NonNull Context context) {
        super(context, SCHEMA_VERSION, FbConfigRoot.class, AppStatusConfig.ID_DATABASE_PATH_CONFIG);
    }
}
