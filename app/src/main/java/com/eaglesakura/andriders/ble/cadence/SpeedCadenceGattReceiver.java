package com.eaglesakura.andriders.ble.cadence;

import com.eaglesakura.andriders.ble.BleDevice;
import com.eaglesakura.andriders.ble.BleDevice.BleDeviceListener;
import com.eaglesakura.andriders.ble.base.BaseBleGattReceiver;
import com.eaglesakura.andriders.ble.cadence.BleCadenceSpeedSensor.BleSpeedCadenceListener;
import com.eaglesakura.andriders.google.FitnessDeviceType;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

/**
 * Speed&Cadence sensor
 */
public class SpeedCadenceGattReceiver extends BaseBleGattReceiver {
    BleSpeedCadenceListener speedCadenceListener;

    public SpeedCadenceGattReceiver(Context context) {
        super(context, FitnessDeviceType.SPEED_CADENCE_SENSOR);
    }

    public void setSpeedCadenceListener(BleSpeedCadenceListener speedCadenceListener) {
        this.speedCadenceListener = speedCadenceListener;
    }

    @Override
    protected BleDevice newBleDevice(BluetoothDevice device) {
        BleCadenceSpeedSensor sensor = new BleCadenceSpeedSensor(context, device);

        sensor.registerCadenceListener(speedCadenceListener);
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
