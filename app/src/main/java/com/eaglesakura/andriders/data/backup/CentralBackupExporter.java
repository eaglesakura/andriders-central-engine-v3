package com.eaglesakura.andriders.data.backup;

import com.eaglesakura.andriders.data.backup.serialize.BackupInformation;
import com.eaglesakura.andriders.data.backup.serialize.SessionBackup;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.error.io.AppIOException;
import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.andriders.storage.AppStorageManager;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.cerberus.error.TaskCanceledException;
import com.eaglesakura.json.JSON;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.IOUtil;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.eaglesakura.sloth.util.AppSupportUtil.assertNotCanceled;

/**
 * バックアップの書込みを行なう
 */
public class CentralBackupExporter {
    Context mContext;

    BackupInformation mBackupInformation = new BackupInformation();

    /**
     * バックアップ情報の拡張子
     */
    static final String FILE_BACKUP_INFORMATION = "information.backup.json";

    /**
     * セッション情報
     */
    static final String EXT_SESSION_DATA = ".session.json";

    /**
     * セッションを格納したパス
     */
    static final String PATH_SESSION_DATA = "sessions/";

    /**
     * バックアップファイルの拡張子
     */
    public static final String EXT_BACKUP_FILE = ".ace3.zip";

    /**
     * バックアップ対象のバージョン
     */
    static final int SUPPORT_BACKUP_VERSION = 1;

    public CentralBackupExporter(Context context) {
        mContext = context;
        mBackupInformation.appVersionName = ContextUtil.getVersionName(context);
        mBackupInformation.deviceName = Build.DEVICE;
        mBackupInformation.exportDate = System.currentTimeMillis();
        mBackupInformation.version = SUPPORT_BACKUP_VERSION;
    }

    /**
     * データを圧縮する
     *
     * @param stream   書込み先ストリーム
     * @param fileName ファイル名
     * @param json     JSON本体
     */
    void compress(ZipOutputStream stream, String fileName, String json) throws IOException {
        ZipEntry entry = new ZipEntry(fileName);
        stream.putNextEntry(entry);
        stream.write(json.getBytes());
        stream.flush();
    }

    /**
     * 外部出力を行なう
     *
     * @param uri            書込み先ファイル
     * @param cancelCallback キャンセルチェック
     * @return タスクが正常終了した場合true
     */
    public void export(@NonNull Callback callback, @NonNull Uri uri, @Nullable CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        // 一時的なファイルに書き込む
        boolean writeCompleted = false;
        File tempFile = Garnet.instance(AppStorageProvider.class, AppStorageManager.class).newTemporaryFile();
        AppLog.system("write -> temp[%s]", tempFile.getAbsolutePath());
        try (OutputStream stream = new FileOutputStream(tempFile)) {
            try (ZipOutputStream zipStream = new ZipOutputStream(stream)) {
                compress(zipStream, FILE_BACKUP_INFORMATION, JSON.encode(mBackupInformation));
                assertNotCanceled(cancelCallback);

                SessionBackup session;
                while ((session = callback.nextSession(this, cancelCallback)) != null) {
                    // セッションをJSON化
                    String json = JSON.encode(session);
                    assertNotCanceled(cancelCallback);

                    // ZIP書き出し
                    String path = PATH_SESSION_DATA + SessionBackup.getSessionId(session) + EXT_SESSION_DATA;
                    compress(zipStream, path, json);
                    assertNotCanceled(cancelCallback);
                }
            }

            // 書込み完了
            writeCompleted = true;
        } catch (IOException e) {
            throw new AppIOException(e);
        } finally {
            if (!writeCompleted) {
                tempFile.delete();
            }
        }

        // 実データに書き込む
        // ここではキャンセルチェックしない
        try (
                OutputStream output = mContext.getContentResolver().openOutputStream(uri);
                InputStream input = new FileInputStream(tempFile);
        ) {
            IOUtil.copyTo(input, false, output, false);
        } catch (IOException e) {
            throw new AppIOException(e);
        } finally {
            // 必ず一時ファイルは削除する
            tempFile.delete();
        }
    }

    public interface Callback {

        /**
         * 次のセッションを列挙する
         */
        @Nullable
        SessionBackup nextSession(CentralBackupExporter self, CancelCallback cancelCallback) throws AppException, TaskCanceledException;
    }
}
