package com.eaglesakura.andriders.ui.binding;

import android.support.annotation.NonNull;

public interface UserLogSynthesis {

    /**
     * 合計走行距離
     */
    @NonNull
    String getSumCyclingDistanceInfo();

    /**
     * 合計獲得標高
     */
    @NonNull
    String getSumAltitudeInfo();

    /**
     * 1日の最長到達距離
     */
    @NonNull
    String getLongestDateDistanceInfo();

    /**
     * 最高速度
     */
    @NonNull
    String getMaxSpeedInfo();

    /**
     * 最高ケイデンス情報
     */
    @NonNull
    String getMaxCadenceInfo();

    /**
     * Exercise値
     */
    @NonNull
    String getExerciseInfo();
}
