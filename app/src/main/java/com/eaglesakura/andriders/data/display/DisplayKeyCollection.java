package com.eaglesakura.andriders.data.display;

import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.collection.DataCollection;

import java.util.List;

/**
 * DisplayKeyの集合を扱う
 */
public class DisplayKeyCollection extends DataCollection<DisplayKey> {
    public DisplayKeyCollection(List<DisplayKey> dataList) {
        super(dataList);
    }

    public DisplayKeyCollection() {
    }
}
