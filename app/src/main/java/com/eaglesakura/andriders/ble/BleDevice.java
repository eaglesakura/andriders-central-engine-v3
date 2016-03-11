package com.eaglesakura.andriders.ble;

import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.bluetooth.BluetoothLeUtil;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class BleDevice {
    /**
     * センサーが停止したと判断するタイムアウト時間
     */
    public static final int SENSOR_TIMEOUT_MS = 1000 * 15;

    /**
     * 接続されたデバイス
     */
    protected BluetoothDevice mDevice;

    /**
     *
     */
    protected final Context mContext;

    /**
     * GATT
     */
    protected BluetoothGatt mBleGatt;

    /**
     * 排他制御用
     */
    protected Object lock = new Object();

    private Set<BleDeviceListener> listeners = new HashSet<>();

    public BleDevice(Context context, BluetoothDevice device) {
        this.mContext = context.getApplicationContext();
        this.mDevice = device;
    }

    public BluetoothGatt getBleGatt() {
        return mBleGatt;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
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
            mBleGatt = mDevice.connectGatt(mContext, false, getCallback());

            AppLog.ble("connect completed(%s)", getClass().getSimpleName());
        }
    }

    /**
     * デバイスから切断する
     */
    public void disconnect() {
        synchronized (lock) {
            gattConnected = false;
            if (mBleGatt != null) {
                mBleGatt.disconnect();
                mBleGatt.close();
                mBleGatt = null;

                AppLog.ble("disconnect completed(%s)", getClass().getSimpleName());
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
            listener.onDeviceConnected(this, mDevice);
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
                    AppLog.ble("onConnectionStateChange connected :: " + mDevice.getName());
                    onGattConneted(gatt);
                    onConnected();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected
                    AppLog.ble("onConnectionStateChange disconnected :: " + mDevice.getName());
                    mBleGatt = null;
                    onDisconnected();
                }
            }
        }
    }

    /**
     * 指定したServiceを持っていればtrue
     */
    protected boolean hasService(UUID serviceUUID) {
        return mBleGatt.getService(serviceUUID) != null;
    }

    protected boolean notificationEnable(UUID serviceUuid, UUID characteristicUuid) {
        BluetoothGattCharacteristic characteristic = BluetoothLeUtil.findBluetoothGattCharacteristic(mBleGatt, serviceUuid, characteristicUuid);
        if (characteristic != null) {
            BluetoothLeUtil.notificationEnable(mBleGatt, characteristic);
            return true;
        } else {
            return false;
        }
    }

    protected boolean notificationDisable(UUID serviceUuid, UUID characteristicUuid) {
        BluetoothGattCharacteristic characteristic = BluetoothLeUtil.findBluetoothGattCharacteristic(mBleGatt, serviceUuid, characteristicUuid);
        if (characteristic != null) {
            BluetoothLeUtil.notificationDisable(mBleGatt, characteristic);
            return true;
        } else {
            return false;
        }
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
