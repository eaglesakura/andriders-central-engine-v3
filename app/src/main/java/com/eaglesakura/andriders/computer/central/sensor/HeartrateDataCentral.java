package com.eaglesakura.andriders.computer.central.sensor;

import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.computer.central.calculator.FitnessDataCalculator;
import com.eaglesakura.andriders.internal.protocol.RawSensorData;
import com.eaglesakura.andriders.sensor.SensorType;

/**
 * センサー情報を集約する
 * <p/>
 * 現在は心拍とケイデンスを管理する
 */
public class HeartrateDataCentral extends SensorDataCentral {
    final RawSensorData.RawHeartrate mHeartrateBuilder = new RawSensorData.RawHeartrate();

    final FitnessDataCalculator mFitnessDataCalculator;

    public HeartrateDataCentral(FitnessDataCalculator fitnessDataCalculator) {
        super(SensorType.HeartrateMonitor);
        this.mFitnessDataCalculator = fitnessDataCalculator;
    }

    /**
     * 有効であればtrue
     */
    public boolean valid() {
        return (System.currentTimeMillis() - mHeartrateBuilder.date) < CentralDataManager.DATA_TIMEOUT_MS;
    }

    /**
     * 現在の心拍を更新する
     */
    public void setHeartrate(int bpm) {
        final long oldTime = mHeartrateBuilder.date;
        final long nowTime = System.currentTimeMillis();

        // 消費カロリー更新
        mFitnessDataCalculator.updateHeartrate(bpm, nowTime - oldTime);

        // 情報更新
        mHeartrateBuilder.bpm = (short) bpm;
        mHeartrateBuilder.date = System.currentTimeMillis();
        mHeartrateBuilder.zone = mFitnessDataCalculator.getZone(bpm);
    }

    @Override
    public void onUpdate(CentralDataManager parent) {
    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}

