package com.eaglesakura.andriders.google;

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

    final DataType dataType;

    final int deviceTypeId;

    private FitnessDeviceType(DataType dataType, int id) {
        this.dataType = dataType;
        this.deviceTypeId = id;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public DataType getFitnessDataType() {
        return dataType;
    }

    public FitnessDeviceController createController(Context context) {
        FitnessDeviceController result = new FitnessDeviceController(context, this);
        return result;
    }
}
