package com.eaglesakura.andriders.central.data.command.proximity;

import com.eaglesakura.andriders.central.data.command.CommandController;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.command.CommandDataCollection;
import com.eaglesakura.andriders.service.command.ProximityData;
import com.eaglesakura.andriders.util.AppLog;

import android.content.Context;
import android.support.annotation.NonNull;

public class ProximityCommandController extends CommandController {

    /**
     * ロードされたコマンド一覧
     */
    @NonNull
    CommandDataCollection mCommands;

    final Object lock = new Object();

    /**
     * 手をかざしたタイミングでの近接情報
     */
    private ProximityData mStartProximity;

    public ProximityCommandController(@NonNull Context context) {
        super(context);
    }

    @NonNull
    private CommandData getCurrentCommand(int timeSec) {
        CommandDataCollection collection = mCommands;
        if (collection == null) {
            return null;
        }

        return collection.find(CommandKey.fromProximity(timeSec));
    }

    /**
     * 近接コマンドリストを更新する
     */
    public void setCommands(@NonNull CommandDataCollection commands) {
        mCommands = commands;
    }

    /**
     * 近接状態が更新された
     */
    public void onUpdate(ProximityData data) {
        if (data.isProximity()) {
            // 近接状態となった
            mStartProximity = data;
        } else {
            // 近接から離された
            if (mStartProximity == null) {
                return;
            }

            long endTime = data.getDate().getTime();
            long startTime = mStartProximity.getDate().getTime();
            AppLog.command("Proximity End[%.1f sec]", (float) (endTime - startTime) / 1000.0f);
            requestCommandBoot(getCurrentCommand((int) (endTime - startTime) / 1000));
        }
    }
}
