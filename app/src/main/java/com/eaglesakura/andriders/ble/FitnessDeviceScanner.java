package com.eaglesakura.andriders.ble;

import com.eaglesakura.android.bluetooth.BluetoothDeviceScanner;
import com.eaglesakura.android.bluetooth.BluetoothDeviceType;

import android.content.Context;

/**
 * 指定したFitnessデバイスの検索を行う
 */
public class FitnessDeviceScanner extends BluetoothDeviceScanner {
    final String targetDeviceAddress;

    final DeviceScanListener scanListener = new DeviceScanListener() {
        @Override
        public void onDeviceFound(BluetoothDeviceScanner self, BluetoothDeviceCache device) {
            if (!device.getAddress().equals(targetDeviceAddress)) {
                // not
                return;
            }

            if (userScanListener != null) {
                userScanListener.onDeviceFound(FitnessDeviceScanner.this, device);
            }
        }

        @Override
        public void onDeviceUpdated(BluetoothDeviceScanner self, BluetoothDeviceCache device) {
            if (!device.getAddress().equals(targetDeviceAddress)) {
                // not
                return;
            }

            if (userScanListener != null) {
                userScanListener.onDeviceUpdated(FitnessDeviceScanner.this, device);
            }
        }

        @Override
        public void onScanTimeout(BluetoothDeviceScanner self) {
            if (userScanListener != null) {
                userScanListener.onScanTimeout(FitnessDeviceScanner.this);
            }
        }
    };

    DeviceScanListener userScanListener;

    public FitnessDeviceScanner(Context context, String targetDeviceAddress) {
        super(context, BluetoothDeviceType.BluetoothLE);
        this.targetDeviceAddress = targetDeviceAddress;
        super.setListener(scanListener);
    }

    @Override
    public void setListener(DeviceScanListener listener) {
        userScanListener = listener;
    }
}
