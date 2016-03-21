package com.eaglesakura.andriders.central.base;

import com.eaglesakura.andriders.central.Clock;
import com.eaglesakura.andriders.db.Settings;

public abstract class BaseCalculator {
    /**
     * 受け取ったデータが無効となるデフォルト時刻
     */
    public static final long DATA_TIMEOUT_MS = 1000 * 15;

    private final Clock mClock;

    public BaseCalculator(Clock clock) {
        mClock = clock;
    }

    /**
     * 現在時刻を取得する
     */
    protected long now() {
        return mClock.now();
    }

    protected Settings getSettings() {
        return Settings.getInstance();
    }
}
