package com.eaglesakura.andriders.central.data.command;

import com.eaglesakura.andriders.model.command.CommandData;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * ACEs実行時のコマンド管理を行う
 */
public abstract class CommandController {
    @NonNull
    final Context mContext;

    CommandBootListener mBootListener;

    public CommandController(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    public void setBootListener(@NonNull CommandBootListener bootListener) {
        mBootListener = bootListener;
    }

    protected void requestCommandBoot(@Nullable CommandData data) {
        CommandBootListener listener = mBootListener;
        if (listener == null || data == null) {
            return;
        }

        listener.onBootCommand(this, data);
    }

    public interface CommandBootListener {

        /**
         * コマンドを起動する
         *
         * MEMO: コールされるスレッドは不定であるため、実装側で適宜調整する
         */
        void onBootCommand(@NonNull CommandController self, @Nullable CommandData data);
    }
}
