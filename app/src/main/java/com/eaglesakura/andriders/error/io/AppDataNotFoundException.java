package com.eaglesakura.andriders.error.io;

/**
 * データが一件も見つからなかった
 */
public class AppDataNotFoundException extends AppDatabaseException {
    public AppDataNotFoundException() {
    }

    public AppDataNotFoundException(String message) {
        super(message);
    }

    public AppDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppDataNotFoundException(Throwable cause) {
        super(cause);
    }
}
