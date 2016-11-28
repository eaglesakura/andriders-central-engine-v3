package com.eaglesakura.andriders.error.plugin;

/**
 * プラグインの処理に失敗した
 */
public class PluginExecuteException extends PluginException {
    public PluginExecuteException() {
    }

    public PluginExecuteException(String message) {
        super(message);
    }

    public PluginExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginExecuteException(Throwable cause) {
        super(cause);
    }
}
