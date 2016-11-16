package com.eaglesakura.andriders.ble.heartrate;

import com.eaglesakura.andriders.ble.base.BaseBleGattReceiver;
import com.eaglesakura.andriders.ble.BleDevice;
import com.eaglesakura.andriders.ble.BleDevice.BleDeviceListener;
import com.eaglesakura.andriders.ble.heartrate.BleHeartrateMonitor.BleHeartrateListener;
import com.eaglesakura.andriders.model.ble.BleDeviceType;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.rx.PendingCallbackQueue;

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

    public HeartrateGattReceiver(Context context, PendingCallbackQueue callbackQueue, Clock clock) {
        super(context, callbackQueue, BleDeviceType.HEARTRATE_MONITOR);
        this.mClock = clock;
    }

    public void setHeartrateListener(BleHeartrateListener heartrateListener) {
        this.mHeartrateListener = heartrateListener;
    }

    @Override
    protected BleDevice newBleDevice(BluetoothDevice device) {
        BleHeartrateMonitor sensor = new BleHeartrateMonitor(mContext, mCallbackQueue, device, mClock);
        sensor.registerHeartrateListener(mHeartrateListener);
        sensor.registerDeviceListener(new BleDeviceListener() {
            @Override
            public void onDeviceConnected(BleDevice self, BluetoothDevice device) {
                AppLog.ble("onDeviceConnected :: %s", self.getDevice().toString());
            }

            @Override
            public void onDeviceDisconnected(BleDevice self) {
                AppLog.ble("onDeviceDisconnected :: %s", self.getDevice().toString());
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
