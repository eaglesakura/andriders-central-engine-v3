package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.AceJUnitTester;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.sensor.HeartrateZone;
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

    @Override
    public void onSetup() {
        super.onSetup();

        // 計算を確定させるため、フィットネスデータを構築する
        // 計算しやすくするため、データはキリの良い数にしておく
        Settings settings = Settings.getInstance();
        settings.getUserProfiles().setUserWeight(65);
        settings.getUserProfiles().setNormalHeartrate(90);
        settings.getUserProfiles().setMaxHeartrate(190);
        settings.getUserProfiles().setWheelOuterLength(2096); // 700 x 23c
    }

    /**
     * セッション開始時刻が正常であることを検証する
     */
    @Test
    public void セッション開始時刻が正確であることを確認する() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        CycleComputerData data = new CycleComputerData(mContext, START_TIME);

        assertNotNull(data.getSessionId());
        assertNotEquals(data.getSessionId(), "");
        assertEquals(data.getStartDate(), START_TIME);

        // 時間を分割して1分経過させる
        for (int i = 0; i < 60; ++i) {
            data.onUpdateTime(1000);
        }
        assertEquals(data.getSessionDulationMs(), 1000 * 60); // データも1分経過している
    }

    /**
     * AACRスタート～折り返し地点を適当な間隔で打刻する
     *
     * 採用している計算法では、2点間距離は約54kmである。
     * 1時間で移動した場合、走行速度は54km/hにならなければならない。
     */
    @Test
    public void GPS座標移動から距離と速度を測定する() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        CycleComputerData data = new CycleComputerData(mContext, START_TIME);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        LogUtil.setOutput(false);
        {
            while (current < 1.0) {
                double lat = MathUtil.blendValue(SAMPLE_START_LATITUDE, SAMPLE_END_LATITUDE, 1.0 - current);
                double lng = MathUtil.blendValue(SAMPLE_START_LONGITUDE, SAMPLE_END_LONGITUDE, 1.0 - current);
                double alt = MathUtil.blendValue(SAMPLE_START_ALTITUDE, SAMPLE_END_ALTITUDE, 1.0 - current);

                final long OFFSET_TIME_MS = (long) ((double) (1000 * 60 * 60) * current);
                final long NOW = (START_TIME + OFFSET_TIME_MS);

                assertTrue(data.setLocation(NOW, lat, lng, alt, Math.random() * 100)); // 現在地点をオフセット
                data.onUpdateTime((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));
                assertFalse(data.isActiveMoving()); // ケイデンスが発生しないので、アクティブにはならないはずである
                assertNotNull(data.getSpeedZone()); // ゾーンは必ず取得できる
                current += OFFSET_TIME_HOUR;

                // 速度をチェックする
                // 時速1kmの誤差を認める
                if (current > 0.1) {
                    assertNotEquals(data.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない
                    assertNotEquals(Math.abs(data.getInclinationPercent()), 0.0, 0.1); // 標高1000mに向かって走るので、傾斜が存在しなければならない
                    assertTrue(data.getSumAltitude() > 0); // 獲得標高がなければならない
                    assertEquals(data.getSpeedKmh(), SAMPLE_DISTANCE_KM, 1.0);
                }
            }
        }
        LogUtil.setOutput(true);

        // 結果だけを出力
        LogUtil.log("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.getDistanceKm(), data.getSpeedKmh(), data.getSpeedZone().name());

        // 約1時間経過していることを確認する
        assertEquals(data.getSessionDulationMs(), 1000 * 60 * 60);

        // 最終的な移動距離をチェックする
        // 1時間分の動作分であるため、ほぼ一致するはずである
        assertEquals(data.getDistanceKm(), data.getSpeedKmh(), 1.0);

        // 獲得標高が目的値とほぼ同等でなければならない
        // MEMO 標高は適当な回数だけ平均を取るので、完全一致はしなくて良い
        assertEquals(data.getSumAltitude(), SAMPLE_END_ALTITUDE, 1.0);
    }

    @Test
    public void 一時間の消費カロリーを計算する() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        CycleComputerData data = new CycleComputerData(mContext, START_TIME);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        LogUtil.setOutput(false);
        {
            while (current < 1.0) {
                final long OFFSET_TIME_MS = (long) ((double) (1000 * 60 * 60) * current);
                long time = (START_TIME + OFFSET_TIME_MS);

                // 心拍140前後をキープさせる
                data.setHeartrate(time, (int) (130.0 + 10.0 * Math.random()));
                data.onUpdateTime((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));

                assertFalse(data.isActiveMoving()); // ケイデンスが発生しないので、アクティブにはならないはずである
                assertNotNull(data.getHeartrateZone()); // ゾーンは必ず取得できる
                assertNotEquals(data.getHeartrateZone(), HeartrateZone.Repose);
                current += OFFSET_TIME_HOUR;
            }
        }
        LogUtil.setOutput(true);

        // 約1時間経過していることを確認する
        assertEquals(data.getSessionDulationMs(), 1000 * 60 * 60);


        // 消費カロリー的には、300～400の間が妥当である
        // 獲得エクササイズは3.5～4.5程度が妥当な値となる
        LogUtil.log("Fitness %.1f kcal / %.1f Ex", data.getSumCalories(), data.getSumExercise());
        assertTrue(data.getSumCalories() > 280);
        assertTrue(data.getSumCalories() < 400);
        assertTrue(data.getSumExercise() > 3.0);
        assertTrue(data.getSumExercise() < 5.0);
    }
}
