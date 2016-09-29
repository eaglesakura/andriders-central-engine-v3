package com.eaglesakura.andriders.model.display;

import com.eaglesakura.andriders.dao.central.DbDisplayTarget;
import com.eaglesakura.andriders.model.DaoModel;

import org.greenrobot.greendao.annotation.NotNull;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * アプリごとのディスプレイ表示内容
 */
public class ApplicationDisplayLayout extends DaoModel<DbDisplayTarget> {
    public ApplicationDisplayLayout(@NonNull DbDisplayTarget raw) {
        super(raw);
    }

    @NotNull
    public String getUniqueId() {
        return mRaw.getUniqueId();
    }

    @NotNull
    public Date getCreatedDate() {
        return mRaw.getCreatedDate();
    }

    @NotNull
    public Date getModifiedDate() {
        return mRaw.getModifiedDate();
    }

    @NotNull
    public String getName() {
        return mRaw.getName();
    }

    public int getLayoutType() {
        return mRaw.getLayoutType();
    }

    public String getTargetPackage() {
        return mRaw.getTargetPackage();
    }
}
