package com.eaglesakura.andriders.plugin;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * コマンド管理用Manager
 */
public class CommandManager {
    @NonNull
    final Context mContext;

    public CommandManager(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }
}
