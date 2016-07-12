package com.eaglesakura.andriders.db.plugin;

import com.eaglesakura.andriders.dao.plugin.DbActivePlugin;

import android.support.annotation.NonNull;

/**
 * 有効になっているプラグイン情報を示す
 */
public class ActivePlugin {
    @NonNull
    DbActivePlugin mRaw;

    public ActivePlugin(@NonNull DbActivePlugin raw) {
        mRaw = raw;
    }

    public String getUniqueId() {
        return mRaw.getUniqueId();
    }

    public String getCategory() {
        return mRaw.getCategory();
    }
}
