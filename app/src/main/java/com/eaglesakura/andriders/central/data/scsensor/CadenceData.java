package com.eaglesakura.andriders.central.data.scsensor;

import com.eaglesakura.andriders.central.data.CycleClock;
import com.eaglesakura.andriders.central.data.base.BaseCalculator;
import com.eaglesakura.andriders.sensor.CadenceZone;
import com.eaglesakura.andriders.v2.db.UserProfiles;

/**
 * ケイデンス情報を保持する
 */
public class CadenceData extends BaseCalculator {
    private float mCadenceRpm;

    private int mCrankRevolution;

    /**
     * 更新時刻
     */
    private long mUpdatedDate;

    public CadenceData(CycleClock clock) {
        super(clock);
    }

    /**
     * データが有効であればtrue
     */
    public boolean valid() {
        return (now() - mUpdatedDate) < DATA_TIMEOUT_MS;
    }

    /**
     * ケイデンスを取得する
     */
    public float getCadenceRpm() {
        if (valid()) {
            return mCadenceRpm;
        } else {
            return 0;
        }
    }

    /**
     * クランクの合計回転数を取得する
     */
    public int getCrankRevolution() {
        return mCrankRevolution;
    }

    /**
     * 現在のケイデンスの状態を指定する
     */
    public CadenceZone getZone() {
        float rpm = getCadenceRpm();
        UserProfiles userProfiles = getSettings().getUserProfiles();
        if (rpm < userProfiles.getCadenceZoneIdeal()) {
            // 遅い
            return CadenceZone.Slow;
        } else if (rpm < userProfiles.getCadenceZoneHigh()) {
            // 理想値
            return CadenceZone.Ideal;
        } else {
            // ハイケイデンス
            return CadenceZone.High;
        }
    }

    /**
     * ケイデンス設定を更新する
     *
     * @return 更新したらtrue
     */
    public boolean setCadence(long timestamp, float crankRpm, int crankRevolution) {
        if (crankRpm < 0 || crankRevolution < 0) {
            return false;
        }

        mCadenceRpm = crankRpm;
        mCrankRevolution = crankRevolution;
        mUpdatedDate = timestamp;
        return true;
    }
}
