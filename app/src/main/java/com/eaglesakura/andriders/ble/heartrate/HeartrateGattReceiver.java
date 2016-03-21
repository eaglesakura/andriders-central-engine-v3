package com.eaglesakura.andriders.ble.heartrate;

import com.eaglesakura.andriders.ble.BleDevice;
import com.eaglesakura.andriders.ble.BleDevice.BleDeviceListener;
import com.eaglesakura.andriders.ble.base.BaseBleGattReceiver;
import com.eaglesakura.andriders.ble.heartrate.BleHeartRateMonitor.BleHeartrateListener;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.google.FitnessDeviceType;
import com.eaglesakura.util.LogUtil;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

public class HeartrateGattReceiver extends BaseBleGattReceiver {

    /**
     * リスナ
     */
    private BleHeartrateListener mHeartrateListener;

    /**
     * 共有時計
     */
    private final Clock mClock;

    public HeartrateGattReceiver(Context context, Clock clock) {
        super(context, FitnessDeviceType.HEARTRATE_MONITOR);
        this.mClock = clock;
    }

    public void setHeartrateListener(BleHeartrateListener heartrateListener) {
        this.mHeartrateListener = heartrateListener;
    }

    @Override
    protected BleDevice newBleDevice(BluetoothDevice device) {
        BleHeartRateMonitor sensor = new BleHeartRateMonitor(context, device, mClock);
        sensor.registerHeartrateListener(mHeartrateListener);
        sensor.registerDeviceListener(new BleDeviceListener() {
            @Override
            public void onDeviceConnected(BleDevice self, BluetoothDevice device) {
                LogUtil.log("Ble onDeviceConnected :: %s", self.getDevice().toString());
            }

            @Override
            public void onDeviceDisconnected(BleDevice self) {
                LogUtil.log("Ble onDeviceDisconnected :: %s", self.getDevice().toString());
            }
        });
        return sensor;
    }

    @Override
    protected void onSensorScanStart() {
    }

    @Override
    protected void onSensorFound(BluetoothDevice device) {
    }
}
