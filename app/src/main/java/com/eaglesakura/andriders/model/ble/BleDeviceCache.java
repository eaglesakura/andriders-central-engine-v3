package com.eaglesakura.andriders.model.ble;

import com.google.android.gms.fitness.data.BleDevice;

import com.eaglesakura.andriders.dao.central.DbBleSensor;
import com.eaglesakura.collection.StringFlag;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Comparator;

/**
 * スキャン済みのデバイス情報をキャッシュする
 */
public class BleDeviceCache {
    @NonNull
    final String mAddress;

    @NonNull
    final String mName;

    @NonNull
    final StringFlag mFlags;

    public BleDeviceCache(@NonNull String address, @NonNull String name, @NonNull StringFlag flags) {
        mAddress = address;
        mName = name;
        mFlags = flags;
    }

    public BleDeviceCache(@NonNull DbBleSensor raw) {
        mAddress = raw.getAddress();
        mName = raw.getName();
        mFlags = StringFlag.parse(raw.getTypeFlags());
    }

    public BleDeviceCache(BleDevice device) {
        mAddress = device.getAddress();
        mName = device.getName();
        mFlags = new StringFlag();
        if (device.getDataTypes().contains(BleDeviceType.HEARTRATE_MONITOR.getFitnessDataType())) {
            mFlags.add(BleDeviceType.ID_HEARTRATE_MONITOR);
        } else if (device.getDataTypes().contains(BleDeviceType.SPEED_CADENCE_SENSOR.getFitnessDataType())) {
            mFlags.add(BleDeviceType.ID_SPEED_AND_CADENCE);
        }
    }

    @NonNull
    public String getAddress() {
        return mAddress;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    /**
     * 表示用の名称を取得する
     */
    @NonNull
    public String getDisplayName(Context context) {
        String address = StringUtil.replaceAllSimple(getAddress(), ":", "").substring(0, 6).toUpperCase();
        return getName() + " ID:" + address;
    }

    @NonNull
    public StringFlag getFlags() {
        return mFlags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleDeviceCache that = (BleDeviceCache) o;

        return mAddress.equals(that.mAddress);

    }

    @Override
    public int hashCode() {
        return mAddress.hashCode();
    }

    public static final Comparator<BleDeviceCache> COMPARATOR_ASC = (a, b) -> StringUtil.compareString(a.getAddress(), b.getAddress());
}
