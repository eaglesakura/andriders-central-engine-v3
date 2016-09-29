package com.eaglesakura.andriders.model.plugin;

import com.eaglesakura.andriders.dao.central.DbActivePlugin;
import com.eaglesakura.andriders.model.DaoModel;

import android.support.annotation.NonNull;

/**
 * 有効になっているプラグイン情報を示す
 */
public class ActivePlugin extends DaoModel<DbActivePlugin> {

    public ActivePlugin(@NonNull DbActivePlugin raw) {
        super(raw);
    }

    public String getUniqueId() {
        return mRaw.getUniqueId();
    }

    public String getCategory() {
        return mRaw.getCategory();
    }
}
