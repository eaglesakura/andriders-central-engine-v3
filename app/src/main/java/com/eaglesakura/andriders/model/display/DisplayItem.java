package com.eaglesakura.andriders.model.display;

import com.eaglesakura.andriders.dao.central.DbDisplayLayout;
import com.eaglesakura.andriders.model.DaoModel;

import android.support.annotation.NonNull;

/**
 * ディスプレイ表示対象のアイテム
 */
public class DisplayItem extends DaoModel<DbDisplayLayout> {
    public DisplayItem(@NonNull DbDisplayLayout raw) {
        super(raw);
    }
}
