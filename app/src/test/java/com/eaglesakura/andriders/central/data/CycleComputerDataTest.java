package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.AceJUnitTester;
import com.eaglesakura.andriders.sensor.SpeedZone;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.MathUtil;
import com.eaglesakura.util.Timer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CycleComputerDataTest extends AceJUnitTester {
    /**
     * サンプルの距離はAACR開始～折り返しとなっている。
     * 計算上、約53km程度となる。
     */
    final double SAMPLE_START_LATITUDE = 36.222704;
    final double SAMPLE_START_LONGITUDE = 137.883526;
    final double SAMPLE_START_ALTITUDE = 100;

    final double SAMPLE_END_LATITUDE = 36.705877;
    final double SAMPLE_END_LONGITUDE = 137.834935;
    final double SAMPLE_END_ALTITUDE = 1100;

    /**
     * サンプルの二点間距離
     *
     * http://keisan.casio.jp/exec/system/1257670779
     */
    final double SAMPLE_DISTANCE_KM = 53.9622;

    @Test
    public void GPS座標移動から距離と速度を測定する() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        CycleComputerData data = new CycleComputerData(mContext, START_TIME);


        // AACRスタート～折り返し地点
        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        LogUtil.setOutput(false);
        {
            while (current < 1.0) {
                double lat = MathUtil.blendValue(SAMPLE_START_LATITUDE, SAMPLE_END_LATITUDE, 1.0 - current);
                double lng = MathUtil.blendValue(SAMPLE_START_LONGITUDE, SAMPLE_END_LONGITUDE, 1.0 - current);
                double alt = MathUtil.blendValue(SAMPLE_START_ALTITUDE, SAMPLE_END_ALTITUDE, 1.0 - current);

                final long OFFSET_TIME_MS = (long) ((double) (1000 * 60 * 60) * current);
                long time = (START_TIME + OFFSET_TIME_MS);

                assertTrue(data.setLocation(time, lat, lng, alt, Math.random() * 100)); // 現在地点をオフセット
                data.onUpdateTime((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));
                assertFalse(data.isActiveMoving()); // ケイデンスが発生しないので、アクティブにはならないはずである
                assertNotNull(data.getSpeedZone()); // ゾーンは必ず取得できる
                current += OFFSET_TIME_HOUR;

                // 速度をチェックする
                // 時速1kmの誤差を認める
                if (current > 0.1) {
                    assertNotEquals(data.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない
                    assertEquals(data.getSpeedKmh(), SAMPLE_DISTANCE_KM, 1.0);
                }
            }
        }
        LogUtil.setOutput(true);

        // 結果だけを出力
        LogUtil.log("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.getDistanceKm(), data.getSpeedKmh(), data.getSpeedZone().name());

        // 最終的な移動距離をチェックする
        // 1時間分の動作分であるため、ほぼ一致するはずである
        assertEquals(data.getDistanceKm(), data.getSpeedKmh(), 1.0);
    }
}
