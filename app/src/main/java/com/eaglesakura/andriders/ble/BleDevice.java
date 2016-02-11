package com.eaglesakura.andriders.ble;

import com.eaglesakura.android.bluetooth.BluetoothLeUtil;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class BleDevice {
    /**
     * 接続されたデバイス
     */
    protected BluetoothDevice device;

    /**
     *
     */
    protected final Context context;

    /**
     * GATT
     */
    protected BluetoothGatt bleGatt;

    /**
     * 排他制御用
     */
    protected Object lock = new Object();

    private Set<BleDeviceListener> listeners = new HashSet<>();

    public BleDevice(Context context, BluetoothDevice device) {
        this.context = context.getApplicationContext();
        this.device = device;
    }

    public BluetoothGatt getBleGatt() {
        return bleGatt;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    /**
     * コールバッククラスを取得する
     */
    protected abstract BluetoothGattCallback getCallback();

    /**
     * デバイスへ接続する
     */
    public void connect() {
        synchronized (lock) {
            gattConnected = false;
            bleGatt = device.connectGatt(context, false, getCallback());

            log("connect completed");
        }
    }

    /**
     * デバイスから切断する
     */
    public void disconnect() {
        synchronized (lock) {
            gattConnected = false;
            if (bleGatt != null) {
                bleGatt.disconnect();
                bleGatt.close();
                bleGatt = null;

                log("disconnect completed");
                onDisconnected();
            }
        }
    }

    /**
     * リスナを登録する
     */
    public void registerDeviceListener(BleDeviceListener listener) {
        listeners.add(listener);
    }

    /**
     * リスナを解放する
     */
    public void unregisterDeviceListener(BleDeviceListener listener) {
        listeners.remove(listener);
    }

    /**
     * デバイスに接続されたら呼び出される
     */
    protected void onConnected() {
        for (BleDeviceListener listener : listeners) {
            listener.onDeviceConnected(this, device);
        }
    }

    /**
     * デバイスから切断されたら呼び出される
     */
    protected void onDisconnected() {
        for (BleDeviceListener listener : listeners) {
            listener.onDeviceDisconnected(this);
        }
    }

    private boolean gattConnected = false;

    public boolean isGattConnected() {
        return gattConnected;
    }

    protected abstract class BaseBluetoothGattCallback extends BluetoothGattCallback {
        public BaseBluetoothGattCallback() {
        }

        /**
         * 接続時のハンドリング
         */
        protected void onGattConneted(BluetoothGatt gatt) {
            // Serviceを検出する
            gatt.discoverServices();

            gattConnected = true;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            synchronized (lock) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    log("onConnectionStateChange connected :: " + device.getName());
                    onGattConneted(gatt);
                    onConnected();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected
                    log("onConnectionStateChange disconnected :: " + device.getName());
                    bleGatt = null;
                    onDisconnected();
                }
            }
        }
    }

    /**
     * 指定したServiceを持っていればtrue
     */
    protected boolean hasService(UUID serviceUUID) {
        return bleGatt.getService(serviceUUID) != null;
    }

    protected boolean notificationEnable(UUID serviceUuid, UUID characteristicUuid) {
        BluetoothGattCharacteristic characteristic = BluetoothLeUtil.findBluetoothGattCharacteristic(bleGatt, serviceUuid, characteristicUuid);
        if (characteristic != null) {
            BluetoothLeUtil.notificationEnable(bleGatt, characteristic);
            return true;
        } else {
            return false;
        }
    }

    protected boolean notificationDisable(UUID serviceUuid, UUID characteristicUuid) {
        BluetoothGattCharacteristic characteristic = BluetoothLeUtil.findBluetoothGattCharacteristic(bleGatt, serviceUuid, characteristicUuid);
        if (characteristic != null) {
            BluetoothLeUtil.notificationDisable(bleGatt, characteristic);
            return true;
        } else {
            return false;
        }
    }

    protected void log(String msg) {
        Log.d("BLE", msg);
    }

    public interface BleDeviceListener {

        /**
         * BLEデバイスに接続された
         */
        void onDeviceConnected(BleDevice self, BluetoothDevice device);

        /**
         * BLEデバイスから切断された
         */
        void onDeviceDisconnected(BleDevice self);
    }
}
