package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.db.command.CommandData;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

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

    void requestCommandBoot(@Nullable CommandData data) {
        CommandBootListener listener = mBootListener;
        if (listener == null) {
            return;
        }

        listener.onBootCommand(this, data);
    }

    @WorkerThread
    public abstract void onUpdate();

    public interface CommandBootListener {

        /**
         * コマンドを起動する
         *
         * MEMO: コールされるスレッドは不定であるため、実装側で適宜調整する
         */
        void onBootCommand(@NonNull CommandController self, @Nullable CommandData data);
    }
}
