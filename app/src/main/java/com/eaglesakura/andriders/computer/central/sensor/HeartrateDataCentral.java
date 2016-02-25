package com.eaglesakura.andriders.computer.central.sensor;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.computer.central.calculator.FitnessDataCalculator;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.internal.protocol.RawSensorData;
import com.eaglesakura.andriders.sensor.SensorType;

/**
 * センサー情報を集約する
 * <p/>
 * 現在は心拍とケイデンスを管理する
 */
public class HeartrateDataCentral extends SensorDataCentral {
    final RawSensorData.RawHeartrate raw = new RawSensorData.RawHeartrate();

    final FitnessDataCalculator mFitnessDataCalculator;

    public HeartrateDataCentral(FitnessDataCalculator fitnessDataCalculator) {
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
        final long oldTime = raw.date;
        final long nowTime = System.currentTimeMillis();

        // 消費カロリー更新
        mFitnessDataCalculator.updateHeartrate(bpm, nowTime - oldTime);

        // 情報更新
        raw.bpm = (short) bpm;
        raw.date = System.currentTimeMillis();
        raw.zone = mFitnessDataCalculator.getZone(bpm);
    }

    @Override
    public void onUpdate(CentralDataManager parent) {
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

