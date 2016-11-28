package com.eaglesakura.andriders.util;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 各種クラスで共有する時計を管理する
 *
 * テスト用途を兼ねているため、時計を現在時刻からズラすこともできる。
 */
public class Clock {
    private AtomicLong mCurrentTime = new AtomicLong();

    public Clock(long currentTime) {
        mCurrentTime.set(currentTime);
    }

    public long now() {
        return mCurrentTime.get();
    }

    public Date nowDate() {
        return new Date(now());
    }

    /**
     * 現在時刻に同期する
     */
    public void sync() {
        mCurrentTime.set(System.currentTimeMillis());
    }

    /**
     * 時刻を上書きする
     *
     * @param time 内部タイマーよりも未来の値
     */
    public void set(long time) {
        if (time < now()) {
            // 整合性を保ちやすくするため、過去には戻せない
            throw new IllegalArgumentException();
        }

        mCurrentTime.set(time);
    }

    /**
     * 差分時間を求める。
     *
     * 絶対値であるため、必ず正の数が返却される。
     */
    public long absDiff(long timestamp) {
        return Math.abs(timestamp - now());
    }

    /**
     * 時計を指定時刻だけ進める
     */
    public void offset(long ms) {
        mCurrentTime.addAndGet(ms);
    }

    private static final Clock sRealtimeClock = new Clock(System.currentTimeMillis()) {
        @Override
        public long now() {
            return System.currentTimeMillis();
        }


        /**
         * 現在時刻リンクのため、オフセットさせることはできない
         * @param ms
         */
        @Override
        public void offset(long ms) {
            throw new IllegalAccessError();
        }
    };

    /**
     * リアルタイム同期用インスタンスを取得する
     */
    public static Clock getRealtimeClock() {
        return sRealtimeClock;
    }
}
