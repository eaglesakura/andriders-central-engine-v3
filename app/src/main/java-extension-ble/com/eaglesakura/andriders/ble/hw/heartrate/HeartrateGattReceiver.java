package com.eaglesakura.andriders.ble.hw.heartrate;

import com.eaglesakura.andriders.ble.hw.BleDevice;
import com.eaglesakura.andriders.ble.hw.BleDevice.BleDeviceListener;
import com.eaglesakura.andriders.ble.hw.base.BaseBleGattReceiver;
import com.eaglesakura.andriders.ble.hw.heartrate._BleHeartrateMonitor.BleHeartrateListener;
import com.eaglesakura.andriders.google.FitnessDeviceType;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.rx.SubscriptionController;

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

    public HeartrateGattReceiver(Context context, SubscriptionController subscriptionController, Clock clock) {
        super(context, subscriptionController, FitnessDeviceType.HEARTRATE_MONITOR);
        this.mClock = clock;
    }

    public void setHeartrateListener(BleHeartrateListener heartrateListener) {
        this.mHeartrateListener = heartrateListener;
    }

    @Override
    protected BleDevice newBleDevice(BluetoothDevice device) {
        _BleHeartrateMonitor sensor = new _BleHeartrateMonitor(mContext, mSubscriptionController, device, mClock);
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
