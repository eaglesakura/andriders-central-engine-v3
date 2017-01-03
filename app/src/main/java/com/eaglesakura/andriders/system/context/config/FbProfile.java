package com.eaglesakura.andriders.system.context.config;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import java.util.List;

/**
 * 各種プロファイルの初期設定
 */
@Keep
public class FbProfile {

    @NonNull
    @Size(min = 1)
    public List<WheelConfig> wheel;

    /**
     * 連携のためのGoogle FitアプリPackage
     */
    @NonNull
    public FbPackageInfo googleFitPackage;


    /**
     * ホイール設定
     */
    public static class WheelConfig {
        /**
         * 表示名
         */
        public String title;

        /**
         * 周長
         */
        public int length;
    }

}
