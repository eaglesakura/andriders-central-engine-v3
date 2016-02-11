package com.eaglesakura.andriders.computer.central.calculator;

import com.eaglesakura.andriders.protocol.SensorProtocol;

/**
 * 運動データの計算を行う
 */
public class FitnessDataCalculator extends BaseCalculator {
    /**
     * 現在のMETs値
     */
    float currentMets;

    /**
     * 合計カロリー
     */
    float sumCalories;

    /**
     * 合計エクササイズ
     */
    float sumExercise;

    public FitnessDataCalculator() {
    }

    public float getCurrentMets() {
        return currentMets;
    }

    public float getUserWeight() {
        return (float) getSettings().getUserProfiles().getUserWeight();
    }

    public int getMaxHeartrate() {
        return getSettings().getUserProfiles().getMaxHeartrate();
    }

    public int getNormalHeartrate() {
        return getSettings().getUserProfiles().getNormalHeartrate();
    }

    public SensorProtocol.RawHeartrate.HeartrateZone getZone(int currentHeartrate) {
        final double userMaxHeartrate = (double) getMaxHeartrate();

        if (currentHeartrate < (int) (userMaxHeartrate * 0.5)) {
            // 安静
            return SensorProtocol.RawHeartrate.HeartrateZone.Repose;
        } else if (currentHeartrate < (int) (userMaxHeartrate * 0.6)) {
            // イージー
            return SensorProtocol.RawHeartrate.HeartrateZone.Easy;
        } else if (currentHeartrate < (int) (userMaxHeartrate * 0.7)) {
            // 脂肪燃焼
            return SensorProtocol.RawHeartrate.HeartrateZone.FatCombustion;
        } else if (currentHeartrate < (int) (userMaxHeartrate * 0.8)) {
            // 有酸素
            return SensorProtocol.RawHeartrate.HeartrateZone.PossessionOxygenMotion;
        } else if (currentHeartrate < (int) (userMaxHeartrate * 0.9)) {
            // 無酸素
            return SensorProtocol.RawHeartrate.HeartrateZone.NonOxygenatedMotion;
        } else {
            // オーバーワーク
            return SensorProtocol.RawHeartrate.HeartrateZone.Overwork;
        }
    }

    /**
     * 差分時間から計算を行う
     */
    public void updateHeartrate(int currentHeartrate, long diffTimeMilliSec) {
        if (diffTimeMilliSec < 500) {
            // 計算に十分な差分時間が無ければ何もしない
            return;
        }

        final int normalHeartrate = getNormalHeartrate();
        final int maxHeartrate = getMaxHeartrate();

        // METsを計算
        if (normalHeartrate <= 0 || maxHeartrate <= 0 || normalHeartrate >= maxHeartrate) {
            // data error
            currentMets = 1;
        } else {
            final float mets = ((float) (currentHeartrate - normalHeartrate) / (float) (maxHeartrate - normalHeartrate) * 10.0f);
            currentMets = Math.max(mets, 1.0f);
        }

        final double diffTimeMs = diffTimeMilliSec;
        // 消費カロリー計算
        {
            // 消費カロリー = METs x 時間(h) x 体重(kg) x 1.05
            // MEMO 先に体重をかけておくことで、精度誤差をマシにする
            double diffTimeHour = ((diffTimeMs * (double) getUserWeight()) / 1000.0 / 60.0 / 60.0);
            double diffCalories = currentMets * diffTimeHour * 1.05;

            sumCalories += diffCalories;
        }

        // 獲得エクササイズ値計算
        // 厚労省データでは3METs以上が活発な値となる
        // http://www.mhlw.go.jp/bunya/kenkou/undou01/pdf/data.pdf
        if (currentMets >= 3) {
            // 獲得エクササイズ値
            double diffExercise = ((currentMets * diffTimeMs) / 1000.0 / 60.0 / 60.0);
            sumExercise += diffExercise;
        }
    }
}
