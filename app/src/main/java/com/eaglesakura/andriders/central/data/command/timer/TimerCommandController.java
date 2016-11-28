package com.eaglesakura.andriders.central.data.command.timer;

import com.eaglesakura.andriders.central.data.command.CommandController;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.util.Timer;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * タイマーコマンドの制御を行う
 */
public class TimerCommandController extends CommandController {
    final CommandData mCommandData;

    /**
     * 次に反応すべき時刻
     */
    long mNextTriggerTime;

    public TimerCommandController(@NonNull Context context, @NonNull CommandData commandData, long nowTimeMS) {
        super(context);
        mCommandData = commandData;

        updateNextTriggerTime(nowTimeMS);
    }

    /**
     * 次に反応すべき時刻を更新する
     */
    private void updateNextTriggerTime(long nowTimeMS) {
        CommandData.Extra extra = mCommandData.getInternalExtra();

        final long INTERVAL_MS = Timer.toMilliSec(0, 0, 0, Math.max(extra.timerIntervalSec, 1), 0);

        if (mNextTriggerTime == 0) {
            // 初回リセット
            if (extra.timerType == CommandData.TIMER_TYPE_SESSION) {
                // 初回はclockからの同期にする
                mNextTriggerTime = nowTimeMS + INTERVAL_MS;
            } else {
                // リアルタイムと同期で調整する
                // 初回実行のみ繰り上げで実行する
                mNextTriggerTime = ((nowTimeMS / INTERVAL_MS) + 1) * INTERVAL_MS;
            }
        } else {
            if ((extra.flags & CommandData.TIMER_FLAG_REPEAT) != 0) {
                // リピートの場合、二度目以降はインターバルを加算するだけでいい
                mNextTriggerTime += INTERVAL_MS;
            } else {
                // リピートしない場合、適当な大きい数を加算することで対応する
                mNextTriggerTime += Timer.toMilliSec(7, 0, 0, 0, 0);    // 1週間後に実行とする。現実的に、もう実行されない。
            }
        }
    }

    /**
     * トリガーを実行する
     */
    private void onTriggerTime() {
        // コマンドの実行リクエストを送る
        requestCommandBoot(mCommandData);
    }

    public void onUpdate(long nowTimeMs) {
        if (nowTimeMs >= mNextTriggerTime) {
            // トリガーを実行
            onTriggerTime();

            // トリガー時刻を更新する
            updateNextTriggerTime(nowTimeMs);
        }
    }
}
