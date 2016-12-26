package com.eaglesakura.andriders.system.context.config;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import java.util.List;

/**
 * 各種プロファイルの初期設定
 */
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
     * GPS情報
     */
    @NonNull
    public GpsConfig gps;

    /**
     * 開発者情報
     */
    @NonNull
    public DeveloperInfo developer;

    /**
     * GPS設定
     */
    public static class GpsConfig {
        @Size(min = 1)
        public List<Integer> accuracyMeter;
    }

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

    /**
     * リンク情報
     */
    public static class Link {
        @NonNull
        public String title;

        @NonNull
        public String linkUrl;

        @Nullable
        public String iconUrl;
    }

    /**
     * 開発者情報
     */
    public static class DeveloperInfo {
        @Size(min = 1)
        public List<Link> link;
    }
}
