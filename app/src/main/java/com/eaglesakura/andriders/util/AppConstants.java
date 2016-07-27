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
    int REQUEST_PICK_GPXFILE = 0x3700;

    /**
     * GoogleFit設定を開く
     */
    int REQUEST_GOOGLE_FIT_SETTING = 0x3701;

    /**
     * Google認証画面を開く
     */
    int REQUEST_GOOGLE_AUTH = 0x3702;

    /**
     * 近接コマンド戻り値
     */
    int REQUEST_COMMAND_SETUP_PROXIMITY = 0x3703;

    /**
     * 速度コマンド戻り値
     *
     * MEMO: 別なFragmentが勝手にハンドリングするのを防ぐため、別々のREQUESTがひつよう
     */
    int REQUEST_COMMAND_SETUP_SPEED = 0x3704;
}

