package com.eaglesakura.andriders.ble.hw.cadence;

import com.eaglesakura.andriders.ble.hw.BleDevice;
import com.eaglesakura.andriders.ble.hw.BleDevice.BleDeviceListener;
import com.eaglesakura.andriders.ble.hw.base.BaseBleGattReceiver;
import com.eaglesakura.andriders.ble.hw.cadence.BleCadenceSpeedSensor.BleSpeedCadenceListener;
import com.eaglesakura.andriders.google.FitnessDeviceType;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.rx.PendingCallbackQueue;
import com.eaglesakura.android.rx.SubscriptionController;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

/**
 * Speed&Cadence sensor
 */
public class SpeedCadenceGattReceiver extends BaseBleGattReceiver {
    BleSpeedCadenceListener mSpeedCadenceListener;

    final Clock mClock;

    public SpeedCadenceGattReceiver(Context context, PendingCallbackQueue callbackQueue, Clock clock) {
        super(context, callbackQueue, FitnessDeviceType.SPEED_CADENCE_SENSOR);
        mClock = clock;
    }

    public void setSpeedCadenceListener(BleSpeedCadenceListener speedCadenceListener) {
        this.mSpeedCadenceListener = speedCadenceListener;
    }

    @Override
    protected BleDevice newBleDevice(BluetoothDevice device) {
        BleCadenceSpeedSensor sensor = new BleCadenceSpeedSensor(mContext, mCallbackQueue, device, mClock);

        sensor.registerCadenceListener(mSpeedCadenceListener);
        // デバイスの接続・切断に対して反応する
        sensor.registerDeviceListener(new BleDeviceListener() {
            @Override
            public void onDeviceConnected(BleDevice self, BluetoothDevice device) {
            }

            @Override
            public void onDeviceDisconnected(BleDevice self) {
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
