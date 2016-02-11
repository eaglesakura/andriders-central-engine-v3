package com.eaglesakura.andriders.computer.central.calculator;

import com.eaglesakura.andriders.protocol.SensorProtocol;

/**
 * 速度ゾーンを計算する
 */
public class SpeedDataCalculator extends BaseCalculator {
    /**
     * 現在の速度
     */
    double speedKmh;

    public SpeedDataCalculator() {
    }

    /**
     * 現在速度を取得する
     */
    public double getSpeedKmh() {
        return speedKmh;
    }

    /**
     * 現在速度を指定する
     */
    public void setSpeedKmh(double speedKmh) {
        this.speedKmh = speedKmh;
    }

    /**
     * 速度ゾーンを取得する
     */
    public SensorProtocol.RawSpeed.SpeedZone getSpeedZone() {
        if (speedKmh < 8) {
            // 適当な閾値よりも下は停止とみなす
            return SensorProtocol.RawSpeed.SpeedZone.Stop;
        } else if (speedKmh < getSettings().getUserProfiles().getSpeedZoneCruise()) {
            // 巡航速度よりも下は低速度域
            return SensorProtocol.RawSpeed.SpeedZone.Slow;
        } else if (speedKmh < getSettings().getUserProfiles().getSpeedZoneSprint()) {
            // スプリント速度よりも下は巡航速度
            return SensorProtocol.RawSpeed.SpeedZone.Cruise;
        } else {
            // スプリント速度を超えたらスプリント
            return SensorProtocol.RawSpeed.SpeedZone.Sprint;
        }
    }

    /**
     * 現在の移動速度から推測した心拍を取得する
     */
    public int getEmulatedHeartrate(float normalHeartrate, float maxHeartrate) {
        return getEmulatedHeartrate(getSpeedZone(), normalHeartrate, maxHeartrate);
    }

    /**
     * 現在の移動速度から推測した心拍を取得する
     */
    public static int getEmulatedHeartrate(SensorProtocol.RawSpeed.SpeedZone zone, float normalHeartrate, float maxHeartrate) {
        float heartrateRange = (maxHeartrate - normalHeartrate);
        switch (zone) {
            case Stop:
                return (int) normalHeartrate;
            case Slow:
                return (int) (normalHeartrate + (heartrateRange * 0.5));
            case Cruise:
                return (int) (normalHeartrate + (heartrateRange * 0.7));
            case Sprint:
                return (int) (normalHeartrate + (heartrateRange * 0.8));
            default:
                return (int) (normalHeartrate + (heartrateRange * 0.9));
        }
    }

}
