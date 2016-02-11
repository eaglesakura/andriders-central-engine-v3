package com.eaglesakura.andriders.computer.central.sensor;

import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.computer.central.calculator.FitnessDataCalculator;
import com.eaglesakura.andriders.protocol.SensorProtocol;

/**
 * センサー情報を集約する
 * <p/>
 * 現在は心拍とケイデンスを管理する
 */
public class HeartrateDataCentral extends SensorDataCentral {
    final SensorProtocol.RawHeartrate.Builder mHeartrateBuilder;

    final FitnessDataCalculator mFitnessDataCalculator;

    public HeartrateDataCentral(FitnessDataCalculator fitnessDataCalculator) {
        super(SensorProtocol.SensorType.HeartrateMonitor);

        this.mHeartrateBuilder = SensorProtocol.RawHeartrate.newBuilder();
        this.mFitnessDataCalculator = fitnessDataCalculator;

        mHeartrateBuilder.setDate(System.currentTimeMillis());
    }

    /**
     * 有効であればtrue
     */
    public boolean valid() {
        return (System.currentTimeMillis() - mHeartrateBuilder.getDate()) < CentralDataManager.DATA_TIMEOUT_MS;
    }

    /**
     * 現在の心拍を更新する
     */
    public void setHeartrate(int bpm) {
        final long oldTime = mHeartrateBuilder.getBpm();
        final long nowTime = System.currentTimeMillis();

        // 消費カロリー更新
        mFitnessDataCalculator.updateHeartrate(bpm, nowTime - oldTime);

        // 情報更新
        mHeartrateBuilder.setBpm(bpm);
        mHeartrateBuilder.setDate(System.currentTimeMillis());
        mHeartrateBuilder.setHeartrateZone(mFitnessDataCalculator.getZone(bpm));
    }

    @Override
    public void onUpdate(CentralDataManager parent) {
    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}

