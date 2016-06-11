package com.eaglesakura.andriders.ble.hw.cadence;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.ble.hw.BleDevice;
import com.eaglesakura.andriders.util.Clock;

import org.junit.Test;

import org.hamcrest.CoreMatchers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SpeedCadenceSensorDataTest extends AppUnitTestCase {

    @Test
    public void _16bit加算値が正常に差分取得できることを確認する() throws Exception {
        assertEquals(BleSpeedCadenceUtil.get16bitOffset(0, 1), 1);
        assertEquals(BleSpeedCadenceUtil.get16bitOffset(0xFFFE, 0), 2);
        assertEquals(BleSpeedCadenceUtil.get16bitOffset(0xFFFF, 0x00010000), 1);
        assertEquals(BleSpeedCadenceUtil.get16bitOffset(1, 0x007FFFF), (0xFFFF - 1));
        assertEquals(BleSpeedCadenceUtil.get16bitOffset(0, 0x1234FFFF), 0xFFFF);

        // 適当な回数だけ加算し、ループを含めて正常に差分値が取得できることを確認する
        int time = 0;
        while (time < (0xFFFF * 2)) {
            int diff = (int) (10.0 + Math.random() * 1000);
            int oldTime = time;
            time += diff;

            assertTrue(BleSpeedCadenceUtil.get16bitOffset(oldTime, time) > 0);
            assertEquals(BleSpeedCadenceUtil.get16bitOffset(oldTime, time), diff);
        }
    }

    @Test
    public void 低精度時計が秒に変換できることを確認する() throws Exception {
        assertEquals(BleSpeedCadenceUtil.sensorTimeToSeconds(1024), 1.0, 0.0001);
        assertEquals(BleSpeedCadenceUtil.sensorTimeToSeconds(1024 * 60), 60.0, 0.0001);
        assertEquals(BleSpeedCadenceUtil.sensorTimeToSeconds(1024 / 2), 0.5, 0.0001);
    }

    @Test
    public void 想定通りの回転値が取得できることを確認する() throws Exception {
        // ランダムで結果が多少上下するので、サンプリング数を上げる
        for (int i = 0; i < 128; ++i) {
            final long START_TIME = System.currentTimeMillis();
            Clock clock = new Clock(START_TIME);
            SpeedCadenceSensorData data = new SpeedCadenceSensorData(clock, BleDevice.SENSOR_TIMEOUT_MS, (int) (1000.0 * 7.5));

            float current = 0;  // 経過時間（分）
            double sensorTime = 12345;    // センサー時間
            double revolveCount = 1234; // 合計回転数

            int sumUpdated = 0;
            double sumRpm = 0;
            final double SAMPLE_RPM = 200.0;
            int oldRevolve = 0;
            boolean validData = false;
            while (current < 60.0) {
                final double OFFSET_MINUTE = (1.0 / 60.0 * (1.5 + Math.random())); // 2.5秒以内の適当なインターバル秒でデータが飛んできていることにする
//            final double SAMPLE_RPM = 190.0 + (Math.random() * 10.0);

                clock.offset((long) (OFFSET_MINUTE * 60.0 * 1000.0));
                sensorTime += (OFFSET_MINUTE * 60.0 * 1024.0);
                revolveCount += (SAMPLE_RPM * OFFSET_MINUTE);
                current += OFFSET_MINUTE;

                // データを流してみる
                if (data.update(((int) revolveCount) & BleSpeedCadenceUtil.SENSOR_16BIT_MASK, ((int) sensorTime) & BleSpeedCadenceUtil.SENSOR_16BIT_MASK)) {
                    // 合計回転数が上がらなければならない
                    assertNotEquals(data.getSumRevolveCount(), oldRevolve);
                    assertThat("SumRevolve :: " + data.getSumRevolveCount(), data.getSumRevolveCount() > oldRevolve, CoreMatchers.is(true));
                    oldRevolve = data.getSumRevolveCount();

                    assertTrue(data.valid());
                    assertNotEquals(data.getRpm(), 0.0);
                    // 誤差があるので適当に上下1割は許容する
                    assertThat("RPM :: " + data.getRpm(), data.getRpm() > (SAMPLE_RPM * 0.9) && data.getRpm() < (SAMPLE_RPM * 1.1), CoreMatchers.is(true));

                    // 平均計算用
                    ++sumUpdated;
                    sumRpm += data.getRpm();

                    validData = true;
                }

                assertEquals(data.valid(), validData);
            }

            // 平均値は誤差を小さく見積もるする
            final double AVG_RPM = (sumRpm / sumUpdated);
            assertThat("AVG RPM :: " + AVG_RPM, AVG_RPM > (SAMPLE_RPM * 0.99) && AVG_RPM < (SAMPLE_RPM * 1.01), CoreMatchers.is(true));
        }
    }
}
