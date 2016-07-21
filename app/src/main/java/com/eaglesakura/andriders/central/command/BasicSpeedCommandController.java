package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.db.command.CommandData;

import android.content.Context;
import android.support.annotation.NonNull;

public class BasicSpeedCommandController extends SpeedCommandController {

    public BasicSpeedCommandController(@NonNull Context context, CommandData data) {
        super(context, data);
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
