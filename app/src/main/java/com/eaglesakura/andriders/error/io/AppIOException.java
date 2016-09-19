package com.eaglesakura.andriders.error.io;

import com.eaglesakura.andriders.error.AppException;

/**
 * アプリの読み書き例外
 */
public class AppIOException extends AppException {
    public AppIOException() {
    }

    public AppIOException(String message) {
        super(message);
    }

    public AppIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppIOException(Throwable cause) {
        super(cause);
    }
}
