package com.eaglesakura.andriders.model.ble;

import com.google.android.gms.fitness.data.DataType;

import android.content.Context;

/**
 * Google Fitで管理されるデバイス
 */
public class BleDeviceType {

    public static final BleDeviceType HEARTRATE_MONITOR;

    public static final BleDeviceType SPEED_CADENCE_SENSOR;

    static {
        HEARTRATE_MONITOR = new BleDeviceType(DataType.TYPE_HEART_RATE_BPM, 0x01);
        SPEED_CADENCE_SENSOR = new BleDeviceType(DataType.TYPE_CYCLING_PEDALING_CADENCE, 0x02);
    }

    final DataType mDataType;

    final int mDeviceTypeId;

    private BleDeviceType(DataType dataType, int id) {
        this.mDataType = dataType;
        this.mDeviceTypeId = id;
    }

    public int getDeviceTypeId() {
        return mDeviceTypeId;
    }

    public DataType getFitnessDataType() {
        return mDataType;
    }

    public BleDeviceScanner createScanner(Context context) {
        BleDeviceScanner result = new BleDeviceScanner(context, this);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleDeviceType that = (BleDeviceType) o;

        if (mDeviceTypeId != that.mDeviceTypeId) return false;
        return mDataType != null ? mDataType.equals(that.mDataType) : that.mDataType == null;

    }

    @Override
    public int hashCode() {
        int result = mDataType != null ? mDataType.hashCode() : 0;
        result = 31 * result + mDeviceTypeId;
        return result;
    }
}
