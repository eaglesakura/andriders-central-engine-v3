package com.eaglesakura.andriders.computer.central.sensor;

import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.protocol.SensorProtocol;
import com.eaglesakura.andriders.v2.db.UserProfiles;

public class CadenceDataCentral extends SensorDataCentral {
    final SensorProtocol.RawCadence.Builder builder = SensorProtocol.RawCadence.newBuilder();

    public CadenceDataCentral() {
        super(SensorProtocol.SensorType.CadenceSensor);

        builder.setDate(System.currentTimeMillis());
    }

    /**
     * ケイデンス設定を更新する
     */
    public void setCadence(float crankRpm, int crankRevolution) {
        if (crankRpm < 0 || crankRevolution < 0) {
            return;
        }
        builder.setRpm((int) crankRpm);
        builder.setCrankRevolution(crankRevolution);
        builder.setDate(System.currentTimeMillis());
        builder.setCadenceZone(getZone());
    }

    /**
     * 有効であればtrue
     */
    public boolean valid() {
        return (System.currentTimeMillis() - builder.getDate()) < CentralDataManager.DATA_TIMEOUT_MS;
    }

    /**
     * 現在のケイデンスの状態を指定する
     */
    public SensorProtocol.RawCadence.CadenceZone getZone() {
        int rpm = builder.getRpm();
        UserProfiles userProfiles = getSettings().getUserProfiles();
        if (rpm < userProfiles.getCadenceZoneIdeal()) {
            // 遅い
            return SensorProtocol.RawCadence.CadenceZone.Slow;
        } else if (rpm < userProfiles.getCadenceZoneHigh()) {
            // 理想値
            return SensorProtocol.RawCadence.CadenceZone.Ideal;
        } else {
            // ハイケイデンス
            return SensorProtocol.RawCadence.CadenceZone.High;
        }
    }

    @Override
    public void onUpdate(CentralDataManager parent) {

    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}
