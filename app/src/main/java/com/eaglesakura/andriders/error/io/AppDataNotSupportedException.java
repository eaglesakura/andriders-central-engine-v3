package com.eaglesakura.andriders.error.io;

/**
 * 非対応のデータだった
 */
public class AppDataNotSupportedException extends AppIOException {
    public AppDataNotSupportedException() {
    }

    public AppDataNotSupportedException(String message) {
        super(message);
    }

    public AppDataNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppDataNotSupportedException(Throwable cause) {
        super(cause);
    }
}
