package com.eaglesakura.andriders.computer.central.data;

/**
 * サイコン内の時計を管理する
 *
 * テスト用途を兼ねているため、時計を現在時刻からズラすこともできる。
 */
public class CycleClock {
    private long mCurrentTime =System.currentTimeMillis();

    public CycleClock(long currentTime) {
        mCurrentTime = currentTime;
    }

    public long now() {
        return mCurrentTime;
    }

    /**
     * 現在時刻に同期する
     */
    public void sync() {
        mCurrentTime = System.currentTimeMillis();
    }

    /**
     * 現在時刻を設定する
     */
    void setCurrentTime(long currentTime) {
        mCurrentTime = currentTime;
    }
}
