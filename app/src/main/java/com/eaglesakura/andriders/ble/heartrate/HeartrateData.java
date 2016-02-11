package com.eaglesakura.andriders.ble.heartrate;

import com.eaglesakura.andriders.ble.SensorUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Timer;

/**
 * 心拍データ
 */
public class HeartrateData {
    /**
     * 心拍BPM
     */
    int bpm = 0;

    /**
     * 最終更新時間
     */
    private Timer lastUpdatedTime = new Timer();

    public HeartrateData() {

    }

    /**
     * 心拍数BPMを取得する
     */
    public int getBpm() {
        synchronized (this) {
            // 指定時間以上回転がなかったらケイデンスをリセットする
            if (lastUpdatedTime.end() > SensorUtil.SENSOR_TIMEOUT_MS) {
                LogUtil.log("reset bpm");
                bpm = 0;
            }

            return bpm;
        }
    }

    /**
     * 心拍数更新を行う
     */
    public boolean update(int newBpm) {
        synchronized (this) {
            lastUpdatedTime.start();
            if (this.bpm != newBpm) {
                this.bpm = newBpm;
                return true;
            } else {
                return false;
            }
        }
    }
}
