package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.util.Timer;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * タイマーコマンドの制御を行う
 */
public class TimerCommandController extends CommandController {
    final CommandData mCommandData;

    final Clock mClock;

    /**
     * 次に反応すべき時刻
     */
    long mNextTriggerTime;

    public TimerCommandController(@NonNull Context context, @NonNull CommandData commandData, @NonNull Clock clock) {
        super(context);
        mClock = clock;
        mCommandData = commandData;

        updateNextTriggerTime();
    }

    /**
     * 次に反応すべき時刻を更新する
     */
    protected void updateNextTriggerTime() {
        CommandData.RawExtra extra = mCommandData.getInternalExtra();

        final long INTERVAL_MS = Timer.toMilliSec(0, 0, 0, Math.max(extra.timerIntervalSec, 1), 0);

        if (mNextTriggerTime == 0) {
            // 初回リセット
            if (extra.timerType == CommandData.TIMERCOMMAND_TYPE_SESSION) {
                // 初回はclockからの同期にする
                mNextTriggerTime = mClock.now() + INTERVAL_MS;
            } else {
                // リアルタイムと同期で調整する
                // 初回実行のみ繰り上げで実行する
                mNextTriggerTime = ((mClock.now() / INTERVAL_MS) + 1) * INTERVAL_MS;
            }
        } else {
            if ((extra.flags & CommandData.TIMERCOMMAND_FLAG_REPEAT) != 0) {
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
    protected void onTriggerTime() {
        // コマンドの実行リクエストを送る
        requestCommandBoot(mCommandData);
    }

    @Override
    public void onUpdate() {
        if (mClock.now() >= mNextTriggerTime) {
            // トリガーを実行
            onTriggerTime();

            // トリガー時刻を更新する
            updateNextTriggerTime();
        }
    }
}
