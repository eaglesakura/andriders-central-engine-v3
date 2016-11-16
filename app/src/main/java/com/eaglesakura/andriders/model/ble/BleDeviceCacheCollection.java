package com.eaglesakura.andriders.model.ble;

import com.eaglesakura.collection.DataCollection;

import java.util.List;

public class BleDeviceCacheCollection extends DataCollection<BleDeviceCache> {
    public BleDeviceCacheCollection(List<BleDeviceCache> dataList) {
        super(dataList);
        setComparator(BleDeviceCache.COMPARATOR_ASC);
    }
}
