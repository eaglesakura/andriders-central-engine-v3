package com.eaglesakura.andriders.data.importer;

import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.cerberus.error.TaskCanceledException;
import com.eaglesakura.lambda.CancelCallback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * セッションデータインポート用抽象クラス
 */
public interface SessionImporter {

    /**
     * データインストールを行う
     */
    void install(@NonNull Listener listener, @Nullable CancelCallback cancelCallback) throws AppException, TaskCanceledException;

    interface Listener {
        void onSessionStart(SessionImporter self, CentralDataManager dataManager) throws AppException;

        void onPointInsert(SessionImporter self, CentralDataManager dataManager, RawCentralData latest) throws AppException;

        void onSessionFinished(SessionImporter self, CentralDataManager dataManager) throws AppException;
    }
}
