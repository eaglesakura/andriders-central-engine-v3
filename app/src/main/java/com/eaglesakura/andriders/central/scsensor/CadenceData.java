package com.eaglesakura.andriders.central.scsensor;

import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.central.base.BaseCalculator;
import com.eaglesakura.andriders.internal.protocol.RawSensorData;
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

    public CadenceData(Clock clock) {
        super(clock);
    }

    public long getUpdatedDate() {
        return mUpdatedDate;
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
     * センサー情報を取得する
     *
     * @return センサー情報を書き込んだ場合true
     */
    public boolean getSensor(RawSensorData dstSensor) {
        if (!valid()) {
            return false;
        }

        dstSensor.cadence = new RawSensorData.RawCadence();
        dstSensor.cadence.date = mUpdatedDate;
        dstSensor.cadence.rpm = (short) getCadenceRpm();
        dstSensor.cadence.crankRevolution = getCrankRevolution();
        dstSensor.cadence.zone = getZone();

        return true;
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
    public boolean setCadence(float crankRpm, int crankRevolution) {
        if (crankRpm < 0 || crankRevolution < 0) {
            return false;
        }

        mCadenceRpm = crankRpm;
        mCrankRevolution = crankRevolution;
        mUpdatedDate = now();
        return true;
    }
}
