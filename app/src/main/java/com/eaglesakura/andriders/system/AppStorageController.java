package com.eaglesakura.andriders.system;

import com.eaglesakura.android.device.external.Storage;
import com.eaglesakura.android.garnet.Singleton;
import com.eaglesakura.util.IOUtil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * アプリのストレージ領域管理を行う
 */
@Singleton
public class AppStorageController {
    @NonNull
    final Context mContext;

    public AppStorageController(@NonNull Context context) {
        mContext = context;
    }

    /**
     * 外部データインストール領域を取得する
     */
    protected File getExternalDataStorage() {
        return Storage.getExternalDataStorage(mContext).getPath();
    }

    /**
     * データベースディレクトリを取得する
     */
    protected File getDatabaseDirectory() {
        File storage = getExternalDataStorage();
        if (storage.getName().equals("files")) {
            storage = storage.getParentFile();
        }
        return new File(storage, "db");
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
     * 外部データベース領域を取得する
     */
    @Nullable
    public File getExternalDatabasePath(@NonNull String name) {
        return new File(getDatabaseDirectory(), name).getAbsoluteFile();
    }
}
