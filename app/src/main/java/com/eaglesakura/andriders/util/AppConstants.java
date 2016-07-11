package com.eaglesakura.andriders.util;

/**
 * Activity/Fragmentから呼び出すリクエストコードを集約する
 *
 * 重複すると、場合によっては意図しないFragmentやActivityがハンドリングしてしまうため、ここでチェックできるようにする。
 */
public interface AppConstants {
    /**
     * GPXファイルを選択する
     */
    int PICK_GPXFILE = 0x3700;

    /**
     * GoogleFit設定を開く
     */
    int GOOGLE_FIT_SETTING = 0x3701;

    /**
     * Google認証画面を開く
     */
    int GOOGLE_AUTH = 0x3702;
}

