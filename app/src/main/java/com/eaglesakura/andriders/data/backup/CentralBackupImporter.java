package com.eaglesakura.andriders.data.backup;

import com.eaglesakura.andriders.data.backup.serialize.BackupInformation;
import com.eaglesakura.andriders.data.backup.serialize.SessionBackup;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.error.io.AppDataNotSupportedException;
import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.andriders.storage.AppStorageManager;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.json.JSON;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.IOUtil;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * バックアップの復元を行なう
 *
 * バックアップはZIPの復元を伴うので
 */
public class CentralBackupImporter {
    Context mContext;

    public CentralBackupImporter(Context context) {
        mContext = context;
    }

    /**
     * ZIPを解凍してディレクトリを返却する
     */
    File unzip(AppStorageManager storageManager, Uri uri, CancelCallback cancelCallback) throws IOException, AppException, TaskCanceledException {
        File tempDir = storageManager.newTemporaryDir();
        try (InputStream input = mContext.getContentResolver().openInputStream(uri)) {
            IOUtil.unzip(input, tempDir, new IOUtil.DecompressCallback() {
                @Override
                public boolean isCanceled() {
                    return CallbackUtils.isCanceled(cancelCallback);
                }

                @Override
                public boolean isDecompressExist(long size, File dst) {
                    return true;
                }

                @Override
                public void onDecompressCompleted(File dst) {

                }
            });
        }
        return tempDir;
    }

    /**
     * ZIPのパースを行なう
     *
     * @param callback       処理コールバック
     * @param uri            処理対象ファイルURI
     * @param cancelCallback キャンセルチェック
     */
    public void parse(Callback callback, Uri uri, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        AppStorageManager storageManager = Garnet.instance(AppStorageProvider.class, AppStorageManager.class);
        File directory = null;
        try {
            directory = unzip(storageManager, uri, cancelCallback);

            // Infoを読み込み
            try (InputStream stream = new FileInputStream(new File(directory, CentralBackupExporter.FILE_BACKUP_INFORMATION))) {
                BackupInformation information = JSON.decode(stream, BackupInformation.class);
                callback.onLoadInformation(this, information, cancelCallback);
            }

            // セッション情報をロード
            for (File file : IOUtil.listFiles(new File(directory, CentralBackupExporter.PATH_SESSION_DATA))) {
                if (!file.getName().endsWith(CentralBackupExporter.EXT_SESSION_DATA)) {
                    continue;
                }

                // セッション情報を見つけた
                AppLog.db("Found Session[%s]", file.getAbsolutePath());
                try (InputStream stream = new FileInputStream(file)) {
                    SessionBackup session = JSON.decode(stream, SessionBackup.class);
                    callback.onLoadSession(this, session, cancelCallback);
                }
            }
        } catch (IOException e) {
            throw new AppDataNotSupportedException(e);
        } finally {
            // キャッシュディレクトリを削除
            if (directory != null) {
                IOUtil.delete(directory);
            }
        }
    }

    public interface Callback {
        /**
         * インフォメーションをロード
         */
        void onLoadInformation(@NonNull CentralBackupImporter self, @NonNull BackupInformation info, CancelCallback cancelCallback) throws AppException, TaskCanceledException;

        /**
         * セッション情報をロード
         */
        void onLoadSession(@NonNull CentralBackupImporter self, @NonNull SessionBackup session, CancelCallback cancelCallback) throws AppException, TaskCanceledException;
    }
}
