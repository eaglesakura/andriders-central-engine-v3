package com.eaglesakura.andriders.ble.heartrate;

import com.eaglesakura.andriders.ble.BleDevice;
import com.eaglesakura.andriders.central.data.Clock;

/**
 * センサーから受け取った直値の心拍データ
 */
public class HeartrateSensorData {
    /**
     * 心拍BPM
     */
    private int mBpm = 0;

    /**
     * 心拍の打刻時間
     */
    private long mUpdatedTime;

    private final Clock mClock;

    public HeartrateSensorData(Clock clock) {
        mClock = clock;
    }

    /**
     * データが有効であればtrue
     */
    public boolean valid() {
        synchronized (this) {
            return mClock.absDiff(mUpdatedTime) < BleDevice.SENSOR_TIMEOUT_MS;
        }
    }

    /**
     * 心拍数BPMを取得する
     */
    public int getBpm() {
        synchronized (this) {
            if (valid()) {
                return mBpm;
            } else {
                return 0;
            }
        }
    }

    /**
     * 心拍数更新を行う
     *
     * 自動的に打刻され、タイムアウト等がチェックされる
     */
    public void setHeartrate(int newBpm) {
        if (newBpm > 250 || newBpm < 0) {
            // 明らかに人間のものではないデータは受け付けない
            throw new IllegalArgumentException("BPM Error :: " + newBpm);
        }
        synchronized (this) {
            mUpdatedTime = mClock.now();
            this.mBpm = newBpm;
        }
    }
}
