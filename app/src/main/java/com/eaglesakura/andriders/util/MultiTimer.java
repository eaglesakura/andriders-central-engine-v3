package com.eaglesakura.andriders.util;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 複数のタイマーを管理・更新する
 */
public class MultiTimer {
    @NonNull
    private final Clock mClock;

    private final Map<Object, ClockTimer> mTimers = new HashMap<>();

    public MultiTimer(@NonNull Clock clock) {
        mClock = clock;
    }

    ClockTimer get(Object id) {
        synchronized (mTimers) {
            ClockTimer result = mTimers.get(id);
            if (result == null) {
                result = new ClockTimer(mClock);
                mTimers.put(id, result);
            }
            return result;
        }
    }

    public void start(Object id) {
        get(id).start();
    }


    /**
     * 指定したタイマーが指定時間を超えていたらタイマーを再スタートさせる
     */
    public boolean endIfOverTime(Object id, long timeMs) {
        ClockTimer timer = get(id);
        if (timer.end() >= timeMs) {
            timer.start();
            return true;
        } else {
            return false;
        }
    }
}
