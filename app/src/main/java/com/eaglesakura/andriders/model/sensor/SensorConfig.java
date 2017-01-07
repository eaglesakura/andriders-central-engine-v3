package com.eaglesakura.andriders.model.sensor;

import com.eaglesakura.andriders.system.context.config.FbSenor;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebaseから同期したセンサーの固定値を取得する
 */
public class SensorConfig {
    @NonNull
    FbSenor mRaw;

    public SensorConfig(FbSenor raw) {
        mRaw = raw;
    }

    /**
     * 信頼するGPS精度一覧を取得する
     */
    @NonNull
    public List<Integer> getGpsAccuracyMeterList() {
        return new ArrayList<>(mRaw.gps.accuracyMeter);
    }
}
