package com.eaglesakura.andriders.error;

import com.eaglesakura.andriders.error.io.AppIOException;
import com.eaglesakura.andriders.error.plugin.PluginException;
import com.eaglesakura.android.rx.error.TaskCanceledException;

import android.os.RemoteException;

import java.io.IOException;

/**
 * アプリ共通例外
 */
public class AppException extends Exception {
    public AppException() {
    }

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppException(Throwable cause) {
        super(cause);
    }

    public static void throwAppException(Throwable e) throws AppException {
        if (e instanceof AppException) {
            throw (AppException) e;
        } else {
            if (e instanceof IOException) {
                throw new AppIOException(e);
            } else if (e instanceof RemoteException) {
                throw new PluginException(e);
            } else {
                throw new AppException(e);
            }
        }
    }

    public static void throwAppExceptionOrTaskCanceled(Throwable e) throws TaskCanceledException, AppException {
        if (e instanceof TaskCanceledException) {
            throw (TaskCanceledException) e;
        } else {
            throwAppException(e);
        }
    }
}
