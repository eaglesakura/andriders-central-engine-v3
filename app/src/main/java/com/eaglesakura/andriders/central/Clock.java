package com.eaglesakura.andriders.central;

/**
 * 各種クラスで共有する時計を管理する
 *
 * テスト用途を兼ねているため、時計を現在時刻からズラすこともできる。
 */
public class Clock {
    private long mCurrentTime;

    public Clock(long currentTime) {
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
        mCurrentTime += ms;
    }

    private static final Clock gRealtimeClock = new Clock(System.currentTimeMillis()) {
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
        return gRealtimeClock;
    }
}
