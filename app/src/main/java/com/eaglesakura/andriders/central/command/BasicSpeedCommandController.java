package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.db.command.CommandDatabase;
import com.eaglesakura.android.rx.SubscriptionController;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

public class BasicSpeedCommandController extends SpeedCommandController {

    /**
     * 基準速度
     */
    final double mSpeedKmh;

    /**
     * コマンドタイプ
     */
    final int mCommandType;


    final CommandData mCommandData;

    public BasicSpeedCommandController(@NonNull Context context, @NonNull SubscriptionController subscriptionController, CommandData data) {
        super(context, subscriptionController);

        Intent intent = data.getInternalIntent();
        mSpeedKmh = intent.getDoubleExtra(CommandData.EXTRA_SPEED_KMH, 25.0);
        mCommandType = intent.getIntExtra(CommandData.EXTRA_SPEED_TYPE, 0);
        mCommandData = data;
    }

    @Override
    void onUpdateSpeed(float currentSpeedKmh) {
        super.onUpdateSpeed(currentSpeedKmh);
        if (mCommandType == CommandData.SPEEDCOMMAND_TYPE_UPPER) {
            // 速度上昇でコマンド
            if (nowSpeedUpper(mSpeedKmh)) {
                requestCommandBoot(mCommandData);
            }
        } else {
            // 速度が下降でコマンド
            if (nowSpeedLower(mSpeedKmh)) {
                requestCommandBoot(mCommandData);
            }
        }
    }

    @Override
    public void onUpdate() {

    }
}
