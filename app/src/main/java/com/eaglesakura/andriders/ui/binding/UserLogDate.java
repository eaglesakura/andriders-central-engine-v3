package com.eaglesakura.andriders.ui.binding;

import android.support.annotation.NonNull;

public interface UserLogDate {
    /**
     * 日付情報を取得する
     */
    @NonNull
    String getDateInfo();

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
