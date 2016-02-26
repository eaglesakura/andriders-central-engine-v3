package com.eaglesakura.andriders.computer.central.sensor;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.internal.protocol.RawSensorData;
import com.eaglesakura.andriders.sensor.CadenceZone;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.andriders.v2.db.UserProfiles;

public class CadenceDataCentral extends SensorDataCentral {
    final RawSensorData.RawCadence mRaw = new RawSensorData.RawCadence();

    public CadenceDataCentral() {
        super(SensorType.CadenceSensor);
    }

    /**
     * ケイデンス設定を更新する
     */
    public void setCadence(float crankRpm, int crankRevolution) {
        if (crankRpm < 0 || crankRevolution < 0) {
            return;
        }
        mRaw.rpm = (short) crankRpm;
        mRaw.crankRevolution = crankRevolution;
        mRaw.date = System.currentTimeMillis();
        mRaw.zone = getZone();
    }

    /**
     * 有効であればtrue
     */
    public boolean valid() {
        return (System.currentTimeMillis() - mRaw.date) < CentralDataManager.DATA_TIMEOUT_MS;
    }

    public int getRpm() {
        if (valid()) {
            return mRaw.rpm;
        } else {
            return 0;
        }
    }

    /**
     * 現在のケイデンスの状態を指定する
     */
    public CadenceZone getZone() {
        int rpm = mRaw.rpm;
        if (!valid()) {
            return CadenceZone.Stop;
        }

        UserProfiles userProfiles = getSettings().getUserProfiles();
        if (rpm < userProfiles.getCadenceZoneIdeal()) {
            // 遅い
            return CadenceZone.Slow;
        } else if (rpm < userProfiles.getCadenceZoneHigh()) {
            // 理想値
            return CadenceZone.Ideal;
        } else {
            // ハイケイデンス
            return CadenceZone.High;
        }
    }

    @Override
    public void onUpdate(CentralDataManager parent) {
        if (!valid()) {
            setCadence(0, 0);
        }
    }

    @Override
    public void buildData(CentralDataManager parent, RawCentralData result) {
        if (valid()) {
            result.sensor.cadence = AceUtils.publicFieldClone(mRaw);
            result.centralStatus.connectedFlags |= RawCentralData.RawCentralStatus.CONNECTED_FLAG_CADENCE_SENSOR;
        }
    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}
