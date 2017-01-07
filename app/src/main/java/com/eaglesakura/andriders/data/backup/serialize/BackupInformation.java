package com.eaglesakura.andriders.data.backup.serialize;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;

/**
 * セッションエクスポートのメタ情報を管理する
 */
@Keep
public class BackupInformation {
    /**
     * 出力日時
     */
    public long exportDate;

    /**
     * アプリバージョン名
     */
    @NonNull
    public String appVersionName;

    /**
     * 出力を行ったデバイス名
     */
    @NonNull
    public String deviceName;

    /**
     * バックアップバージョン
     */
    public int version;
}
