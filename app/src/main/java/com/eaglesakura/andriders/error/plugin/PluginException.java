package com.eaglesakura.andriders.error.plugin;

import com.eaglesakura.andriders.error.AppException;

/**
 * プラグイン関連の例外処理
 */
public class PluginException extends AppException {
    public PluginException() {
    }

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginException(Throwable cause) {
        super(cause);
    }
}
