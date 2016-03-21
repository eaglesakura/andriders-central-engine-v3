package com.eaglesakura.andriders.ble.heartrate;

import com.eaglesakura.andriders.AceJUnitTester;
import com.eaglesakura.andriders.ble.BleDevice;
import com.eaglesakura.andriders.central.Clock;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * 心拍データ管理テスト
 */
public class SensorHeartrateDataTest extends AceJUnitTester {

    @Test
    public void 心拍がタイムアウトすることを確認する() throws Exception {
        Clock clock = new Clock(System.currentTimeMillis());
        HeartrateSensorData sensor = new HeartrateSensorData(clock);

        // 1分間心拍を送る
        for (int i = 0; i < 60; ++i) {
            sensor.setHeartrate((int) (100.0 + Math.random() * 50));
            clock.offset(1000);
        }

        assertNotEquals(sensor.getBpm(), 0);
        assertTrue(sensor.getBpm() < 250);
        assertTrue(sensor.valid());

        // 時計を進めて、センサーがタイムアウトすることを確認
        // タイムアウト後の心拍は0である。
        clock.offset(BleDevice.SENSOR_TIMEOUT_MS);
        assertFalse(sensor.valid());
        assertEquals(sensor.getBpm(), 0);
    }
}

