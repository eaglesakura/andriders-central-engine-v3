package com.eaglesakura.andriders.service.central;

import com.eaglesakura.andriders.central.command.CommandController;
import com.eaglesakura.andriders.command.SerializableIntent;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.serialize.RawIntent;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.PendingCallbackQueue;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * コマンド実行の実装本体
 */
class CommandBootListenerImpl implements CommandController.CommandBootListener {

    @NonNull
    Context mContext;

    @NonNull
    PendingCallbackQueue mCallbackQueue;

    public CommandBootListenerImpl(@NonNull Context context, @NonNull PendingCallbackQueue callbackQueue) {
        mContext = context.getApplicationContext();
        mCallbackQueue = callbackQueue;
    }

    @Override
    public void onBootCommand(@NonNull CommandController self, @Nullable CommandData data) {
        if (data == null) {
            return;
        }

        mCallbackQueue.run(ObserveTarget.Alive, () -> {
            try {
                RawIntent rawIntent = data.getIntent();
                Intent intent = SerializableIntent.newIntent(rawIntent);
                switch (rawIntent.intentType) {
                    case Activity:
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 新規Taskでなければならない
                        mContext.startActivity(intent);
                        break;
                    case Service:
                        mContext.startService(intent);  // Serviceを開始
                        break;
                    case Broadcast:
                        intent.setPackage(data.getPackageName());   // 対象packageを固定する
                        mContext.sendBroadcast(intent);             // Brodacastを投げる
                        break;
                }
            } catch (Exception e) {
                AppLog.printStackTrace(e);
            }
        });
    }
}
