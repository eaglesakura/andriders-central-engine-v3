package com.eaglesakura.andriders.ble.cadence;

/**
 * ケイデンスセンサー内蔵の時間をカウントする
 * 分解能はBLE C&Sセンサーの仕様にしたがって1/1024
 */
public class SensorTime {
    int time;

    public SensorTime() {
    }

    public void reset(int time) {
        this.time = (time & 0xFFFF);
    }

    /**
     * 記録されている時間からのオフセット時間（秒）を取得する
     */
    public double offsetTimeMs(int after) {
        if (after < time) {
            // 一巡している場合、Afterを調整する
            after += 0x0000FFFF;
        }

        final int offset = after - time;
        return ((double) offset) / 1024.0 * 1000.0;
    }
}
