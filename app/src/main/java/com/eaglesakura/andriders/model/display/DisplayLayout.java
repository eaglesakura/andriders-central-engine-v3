package com.eaglesakura.andriders.model.display;

import com.eaglesakura.andriders.dao.central.DbDisplayLayout;
import com.eaglesakura.andriders.model.DaoModel;

import org.greenrobot.greendao.annotation.NotNull;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * ディスプレイ表示対象のアイテム
 */
public class DisplayLayout extends DaoModel<DbDisplayLayout> {

    /**
     * デフォルトパッケージ名
     * 現実的に存在し得ない。
     */
    private static final String PACKAGE_NAME_DEFAULT = ".null";

    public DisplayLayout(@NonNull DbDisplayLayout raw) {
        super(raw);
    }

    @NotNull
    public String getUniqueId() {
        return mRaw.getUniqueId();
    }

    @NotNull
    public Date getUpdatedDate() {
        return mRaw.getUpdatedDate();
    }

    @NotNull
    public String getAppPackageName() {
        return mRaw.getAppPackageName();
    }

    /**
     * スロット位置を一意に識別可能なIDを取得する
     */
    public int getSlotId() {
        return mRaw.getSlotId();
    }

    @NotNull
    public String getPluginId() {
        return mRaw.getPluginId();
    }

    @NotNull
    public String getValueId() {
        return mRaw.getValueId();
    }


}
