package com.eaglesakura.andriders.storage;

import com.eaglesakura.android.garnet.Singleton;
import com.eaglesakura.util.IOUtil;
import com.eaglesakura.util.RandomUtil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

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
    protected File getDataStoragePath() {
//        return Storage.getDataStoragePath(mContext).getPath();
        return mContext.getExternalFilesDir(null);
    }

    /**
     * データベースディレクトリを取得する
     */
    protected File getDatabaseDirectory() {
        File storage = getDataStoragePath();
        if (storage.getName().equals("files")) {
            storage = storage.getParentFile();
        }
        return IOUtil.mkdirs(new File(storage, "db"));
    }

    /**
     * 外部データベース領域を取得する
     */
    @Nullable
    public File getExternalDatabasePath(@NonNull String name) {
        return new File(getDatabaseDirectory(), name).getAbsoluteFile();
    }

    /**
     * 新たなキャッシュファイルを生成する。
     * このファイルは内部領域に作られ、次回起動時に削除される。
     */
    @NonNull
    public File newTemporaryFile() {
        File cacheDir = mContext.getCacheDir();
        return new File(cacheDir, "" + System.currentTimeMillis() + "-" + RandomUtil.randShortString() + ".bin");
    }

    /**
     * 新たなキャッシュディレクトリを生成する
     */
    @NonNull
    public File newTemporaryDir() {
        File cacheDir = mContext.getCacheDir();
        File result = new File(cacheDir, "" + System.currentTimeMillis() + "-" + RandomUtil.randShortString());
        result.mkdirs();
        return result;
    }
}
