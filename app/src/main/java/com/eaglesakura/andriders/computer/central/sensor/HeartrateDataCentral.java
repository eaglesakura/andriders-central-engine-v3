package com.eaglesakura.andriders.computer.central.sensor;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.computer.central.data.hrsensor.FitnessData;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.internal.protocol.RawSensorData;
import com.eaglesakura.andriders.sensor.HeartrateZone;
import com.eaglesakura.andriders.sensor.SensorType;

/**
 * センサー情報を集約する
 * <p/>
 * 現在は心拍とケイデンスを管理する
 */
public class HeartrateDataCentral extends SensorDataCentral {
    final RawSensorData.RawHeartrate raw = new RawSensorData.RawHeartrate();

    final FitnessData mFitnessDataCalculator;

    public HeartrateDataCentral(FitnessData fitnessDataCalculator) {
        super(SensorType.HeartrateMonitor);
        this.mFitnessDataCalculator = fitnessDataCalculator;
    }

    /**
     * 有効であればtrue
     */
    public boolean valid() {
        return (System.currentTimeMillis() - raw.date) < CentralDataManager.DATA_TIMEOUT_MS;
    }

    /**
     * 現在の心拍を更新する
     */
    public void setHeartrate(int bpm) {

//        mFitnessDataCalculator.setHeartrate(bpm);

        // 情報更新
        raw.bpm = (short) bpm;
        raw.date = System.currentTimeMillis();
    }

    @Override
    public void onUpdate(CentralDataManager parent, long diffTimeMs) {
        if (!valid()) {
            raw.bpm = 0;
            raw.date = 0;
            raw.zone = HeartrateZone.Repose;
        } else {
//            mFitnessDataCalculator.setHeartrate(raw.bpm);
            raw.zone = mFitnessDataCalculator.getZone();
        }

    }

    @Override
    public void buildData(CentralDataManager parent, RawCentralData result) {
        if (valid()) {
            result.sensor.heartrate = AceUtils.publicFieldClone(raw);
            result.centralStatus.connectedFlags |= RawCentralData.RawCentralStatus.CONNECTED_FLAG_HEARTRATE_SENSOR;
        }
    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}

