package com.eaglesakura.andriders.computer.central.sensor;

import com.eaglesakura.andriders.computer.central.base.BaseCentral;
import com.eaglesakura.andriders.protocol.SensorProtocol;

public abstract class SensorDataCentral extends BaseCentral {
    /**
     * 識別用センサーデータ
     */
    final SensorProtocol.SensorType mType;

    public SensorDataCentral(SensorProtocol.SensorType type) {
        this.mType = type;
    }

    public SensorProtocol.SensorType getType() {
        return mType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorDataCentral that = (SensorDataCentral) o;

        return mType == that.mType;

    }

    @Override
    public int hashCode() {
        return mType != null ? mType.hashCode() : 0;
    }
}
