package com.eaglesakura.andriders.error;

import com.eaglesakura.andriders.error.io.AppIOException;
import com.eaglesakura.andriders.error.plugin.PluginException;
import com.eaglesakura.cerberus.error.TaskCanceledException;

import android.os.RemoteException;

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * アプリ共通例外
 */
public class AppException extends AceException {
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

    static Throwable getInternal(Throwable e) {
        // RuntimeExceptionでラップされている場合、内部を確認する
        if (e instanceof RuntimeException) {
            while (e.getCause() != null && !(e.getCause() instanceof RuntimeException)) {
                e = e.getCause();
            }
        }

        return e;
    }

    public static void throwAppException(Throwable e) throws AppException {

        // RuntimeExceptionでラップされている場合、内部を確認する
        e = getInternal(e);

        if (e instanceof AppException) {
            throw (AppException) e;
        } else {
            if (e instanceof Error) {
                // Errorのハンドリングはそれに任せる
                throw (Error) e;
            } else if (e instanceof IOException) {
                throw new AppIOException(e);
            } else if (e instanceof RemoteException) {
                throw new PluginException(e);
            } else {
                throw new AppException(e);
            }
        }
    }

    public static void throwAppExceptionOrTaskCanceled(Throwable e) throws TaskCanceledException, AppException {
        e = getInternal(e);

        if (e instanceof TaskCanceledException) {
            throw (TaskCanceledException) e;
        } else if (e instanceof InterruptedIOException) {
            throw new TaskCanceledException(e);
        } else {
            throwAppException(e);
        }
    }
}
