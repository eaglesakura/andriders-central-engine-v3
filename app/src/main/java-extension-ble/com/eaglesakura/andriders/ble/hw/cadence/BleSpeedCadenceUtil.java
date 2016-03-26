package com.eaglesakura.andriders.ble.hw.cadence;

/**
 *
 */
public class BleSpeedCadenceUtil {

    /**
     * 16bitデータ用のマスク値
     */
    static final int SENSOR_16BIT_MASK = 0x0000FFFF;

    /**
     * 16bit値がオーバーフローしていたらtrue
     *
     * @param oldValue 古い値
     * @param newValue 新しい値
     */
    public static boolean is16bitOverflow(int oldValue, int newValue) {
        return (oldValue & SENSOR_16BIT_MASK) > (newValue & SENSOR_16BIT_MASK);
    }

    /**
     * 差分を取得する
     *
     * 16bit循環でnewTime < oldTimeになった場合は内部で値を調整する
     *
     * @param oldValue 古い値
     * @param newValue 新しい値
     * @return 差分
     */
    public static int get16bitOffset(int oldValue, int newValue) {
        oldValue &= SENSOR_16BIT_MASK;
        newValue &= SENSOR_16BIT_MASK;

        if (newValue < oldValue) {
            // newTimeが循環していたら、値を1順進める
            newValue += (SENSOR_16BIT_MASK + 1);
        }
        return newValue - oldValue;
    }

    /**
     * 低精度時計データを秒に変換する
     * 1024 = 1.0秒となる。16bitが最大値のため、約65秒で1順する。
     */
    public static double sensorTimeToSeconds(int sensorTime) {
        sensorTime &= SENSOR_16BIT_MASK;
        return (double) sensorTime / 1024.0;
    }
}
