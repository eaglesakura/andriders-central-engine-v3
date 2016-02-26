package com.eaglesakura.andriders.central.data.base;

import com.eaglesakura.andriders.central.data.CycleClock;
import com.eaglesakura.andriders.db.Settings;

public abstract class BaseCalculator {
    /**
     * 受け取ったデータが無効となるデフォルト時刻
     */
    public static final long DATA_TIMEOUT_MS = 1000 * 15;

    private final CycleClock mClock;

    public BaseCalculator(CycleClock clock) {
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
