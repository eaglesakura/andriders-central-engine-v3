package com.eaglesakura.andriders.model.display;

import com.eaglesakura.collection.DataCollection;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * 対象となるアプリごとのディスプレイレイアウト
 */
public class DisplayLayoutCollection extends DataCollection<DisplayLayout> {
    public DisplayLayoutCollection(List<DisplayLayout> dataList) {
        super(dataList);
    }

    /**
     * 指定スロットのレイアウトを検索する
     */
    @Nullable
    public DisplayLayout find(int slotId) {
        return find(it -> it.getSlotId() == slotId);
    }


}
