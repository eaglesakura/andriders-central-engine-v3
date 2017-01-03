package com.eaglesakura.andriders.system.context.config;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import java.util.List;

/**
 * センサー情報を含んだConfig値
 */
@Keep
public class FbSenor {
    /**
     * GPS情報
     */
    @NonNull
    public GpsConfig gps;

    /**
     * GPS設定
     */
    public static class GpsConfig {
        @Size(min = 1)
        public List<Integer> accuracyMeter;
    }
}
