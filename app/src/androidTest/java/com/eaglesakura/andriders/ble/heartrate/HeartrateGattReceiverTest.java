package com.eaglesakura.andriders.ble.heartrate;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.thread.IntHolder;

import org.junit.Test;

import android.bluetooth.BluetoothDevice;

/**
 *
 */
public class HeartrateGattReceiverTest extends AppDeviceTestCase {

    @Test
    public void BLE機器に接続できる() throws Throwable {
        ServiceLifecycleDelegate delegate = new ServiceLifecycleDelegate();
        HeartrateGattReceiver receiver = new HeartrateGattReceiver(getContext(), delegate.getCallbackQueue(), Clock.getRealtimeClock());
        try {
            delegate.onCreate();

            IntHolder updateCount = new IntHolder();
            receiver.setTargetFitnessDeviceAddress("CE:16:3A:86:48:F9");
            receiver.setHeartrateListener(new BleHeartrateMonitor.BleHeartrateListener() {
                @Override
                public void onDeviceNotSupportedHeartrate(BleHeartrateMonitor sensor, BluetoothDevice device) {

                }

                @Override
                public void onDeviceSupportedHeartrate(BleHeartrateMonitor sensor, BluetoothDevice device) {

                }

                @Override
                public void onHeartrateUpdated(BleHeartrateMonitor sensor, HeartrateSensorData heartrate) {
                    updateCount.add(1);
                    AppLog.test("Received BLE[%d bpm]", heartrate.getBpm());
                }
            });
            awaitUiThread(() -> receiver.connect());

            // 適当な回数アップデートされるまで待つ
            while (updateCount.value < 60) {
                sleep(1);
            }
        } finally {
            awaitUiThread(()->receiver.disconnect());
            delegate.onDestroy();
        }
    }
}