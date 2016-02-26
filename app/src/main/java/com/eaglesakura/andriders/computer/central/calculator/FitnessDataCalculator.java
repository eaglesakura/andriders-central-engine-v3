package com.eaglesakura.andriders.computer.central.calculator;

import com.eaglesakura.andriders.sensor.HeartrateZone;

/**
 * 運動データの計算を行う
 */
public class FitnessDataCalculator extends BaseCalculator {
    /**
     * 現在のMETs値
     */
    private float mCurrentMets;

    /**
     * 合計カロリー
     */
    private float mSumCalories;

    /**
     * 合計エクササイズ
     */
    private float mSumExercise;

    public FitnessDataCalculator() {
    }

    public float getCurrentMets() {
        return mCurrentMets;
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

    public HeartrateZone getZone(int currentHeartrate) {
        final double userMaxHeartrate = (double) getMaxHeartrate();

        if (currentHeartrate < (int) (userMaxHeartrate * 0.5)) {
            // 安静
            return HeartrateZone.Repose;
        } else if (currentHeartrate < (int) (userMaxHeartrate * 0.6)) {
            // イージー
            return HeartrateZone.Easy;
        } else if (currentHeartrate < (int) (userMaxHeartrate * 0.7)) {
            // 脂肪燃焼
            return HeartrateZone.FatCombustion;
        } else if (currentHeartrate < (int) (userMaxHeartrate * 0.8)) {
            // 有酸素
            return HeartrateZone.PossessionOxygenMotion;
        } else if (currentHeartrate < (int) (userMaxHeartrate * 0.9)) {
            // 無酸素
            return HeartrateZone.NonOxygenatedMotion;
        } else {
            // オーバーワーク
            return HeartrateZone.Overwork;
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
            mCurrentMets = 1;
        } else {
            final float mets = ((float) (currentHeartrate - normalHeartrate) / (float) (maxHeartrate - normalHeartrate) * 10.0f);
            mCurrentMets = Math.max(mets, 1.0f);
        }

        final double diffTimeMs = diffTimeMilliSec;
        // 消費カロリー計算
        {
            // 消費カロリー = METs x 時間(h) x 体重(kg) x 1.05
            // MEMO 先に体重をかけておくことで、精度誤差をマシにする
            double diffTimeHour = ((diffTimeMs * (double) getUserWeight()) / 1000.0 / 60.0 / 60.0);
            double diffCalories = mCurrentMets * diffTimeHour * 1.05;

            mSumCalories += diffCalories;
        }

        // 獲得エクササイズ値計算
        // 厚労省データでは3METs以上が活発な値となる
        // http://www.mhlw.go.jp/bunya/kenkou/undou01/pdf/data.pdf
        if (mCurrentMets >= 3) {
            // 獲得エクササイズ値
            double diffExercise = ((mCurrentMets * diffTimeMs) / 1000.0 / 60.0 / 60.0);
            mSumExercise += diffExercise;
        }
    }
}
