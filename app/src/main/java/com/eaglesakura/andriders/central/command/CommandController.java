package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.SubscriptionController;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

/**
 * ACEs実行時のコマンド管理を行う
 */
public abstract class CommandController {
    @NonNull
    final Context mContext;
    @NonNull
    final SubscriptionController mSubscriptionController;

    CommandBootListener mBootListener;

    public CommandController(@NonNull Context context, @NonNull SubscriptionController subscriptionController) {
        mContext = context.getApplicationContext();
        mSubscriptionController = subscriptionController;
    }

    public void setBootListener(@NonNull CommandBootListener bootListener) {
        mBootListener = bootListener;
    }

    void requestCommandBoot(@Nullable CommandData data) {
        CommandBootListener listener = mBootListener;
        if (listener == null) {
            return;
        }

        mSubscriptionController.run(ObserveTarget.Alive, () -> {
            listener.onBootCommand(this, data);
        });
    }

    @WorkerThread
    public abstract void onUpdate();

    public interface CommandBootListener {
        @UiThread
        void onBootCommand(@NonNull CommandController self, @Nullable CommandData data);
    }
}
