package com.eaglesakura.andriders.model.ble;

import com.google.android.gms.fitness.data.DataType;

import android.content.Context;

/**
 * Google Fitで管理されるデバイス
 */
public class FitnessDeviceType {

    public static final FitnessDeviceType HEARTRATE_MONITOR;

    public static final FitnessDeviceType SPEED_CADENCE_SENSOR;

    static {
        HEARTRATE_MONITOR = new FitnessDeviceType(DataType.TYPE_HEART_RATE_BPM, 0x01);
        SPEED_CADENCE_SENSOR = new FitnessDeviceType(DataType.TYPE_CYCLING_PEDALING_CADENCE, 0x02);
    }

    final DataType mDataType;

    final int mDeviceTypeId;

    private FitnessDeviceType(DataType dataType, int id) {
        this.mDataType = dataType;
        this.mDeviceTypeId = id;
    }

    public int getDeviceTypeId() {
        return mDeviceTypeId;
    }

    public DataType getFitnessDataType() {
        return mDataType;
    }

    public FitnessDeviceScanner createScanner(Context context) {
        FitnessDeviceScanner result = new FitnessDeviceScanner(context, this);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FitnessDeviceType that = (FitnessDeviceType) o;

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
