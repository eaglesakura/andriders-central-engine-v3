package com.eaglesakura.andriders.db;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.android.device.external.StorageInfo;
import com.eaglesakura.android.garnet.Singleton;
import com.eaglesakura.util.IOUtil;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.List;

/**
 * アプリのストレージ領域管理を行う
 */
@Singleton
public class AppStorageManager {
    @NonNull
    final Context mContext;

    public AppStorageManager(@NonNull Context context) {
        mContext = context;
    }

    /**
     * 外部データインストール領域を取得する
     */
    protected File getExternalDataStorage() {
        List<StorageInfo> externalStorages = StorageInfo.listExternalStorages();
        File storageRoot = externalStorages.get(0).getPath();
        if (storageRoot == null) {
            storageRoot = Environment.getExternalStorageDirectory();
        }
        return new File(storageRoot, "andriders/" + BuildConfig.APPLICATION_ID);
    }

    protected File getDatabaseDirectory() {
        return new File(getExternalDataStorage(), "db");
    }

    /**
     * アプリ用ディレクトリを生成し、成功したらtrueを返却する
     */
    public boolean makeDirectory() {
        File root = getExternalDataStorage();
        if (root.isDirectory()) {
            return true;
        }

        return IOUtil.mkdirs(root).isDirectory();
    }

    /**
     * データベース領域を取得する
     *
     * MEMO: KitkatはSDカードアクセスが行えないため、内部ストレージ限定とする。
     *
     * この場合、Kitkat -> Lollipopアップデートでlogを見失う場合があるが、
     * 2016年現在Kitkatのママの端末はLollipopへのアップデートを行わない可能性が高いため、
     * 問題は軽微であると考えて無視する。
     */
    @Nullable
    public String getDatabasePath(@NonNull String name) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            return mContext.getDatabasePath(name).getAbsolutePath();
        } else {
            return new File(getDatabaseDirectory(), name).getAbsolutePath();
        }
    }
}
