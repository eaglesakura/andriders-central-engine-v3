package com.eaglesakura.andriders.ble.heartrate;

import com.eaglesakura.andriders.ble.BleDevice;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.bluetooth.BluetoothLeUtil;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.PendingCallbackQueue;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BleHeartrateMonitor extends BleDevice {

    /**
     * ハートレートモニターのBLEサービスを示すUUID
     * <br>
     * 基本は0000XXXX-0000-1000-8000-00805f9b34fb
     * <br>
     * 参考：
     * https://developer.bluetooth.org/gatt/services/Pages/ServiceViewer.aspx?u=org.bluetooth.service.heart_rate.xml
     */
    public static final UUID BLE_UUID_SERVICE_HEARTRATE = BluetoothLeUtil.createUUIDFromAssignedNumber("0x180d");


    /**
     * 心拍値を示すUUID
     */
    public static final UUID BLE_UUID_HEARTRATE_MEASUREMENT = BluetoothLeUtil.createUUIDFromAssignedNumber("0x2a37");

    /**
     * バッテリーサービスを示すUUID
     *
     * 参考: http://stackoverflow.com/questions/19539535/how-to-get-the-battery-level-after-connect-to-the-ble-device
     */
    public static final UUID BLE_UUID_SERVICE_BATTERY = BluetoothLeUtil.createUUIDFromAssignedNumber("0x180f");

    /**
     * バッテリー残量を示すUUID
     */
    public static final UUID BLE_UUID_BATTERY_LEVEL = BluetoothLeUtil.createUUIDFromAssignedNumber("0x2a19");

    /**
     * 心拍リスナ
     */
    @NonNull
    private final Set<BleHeartrateListener> mListeners = new HashSet<>();

    /**
     * 心拍データ
     */
    @NonNull
    private final HeartrateSensorData mHeartrateData;

    @NonNull
    private final PendingCallbackQueue mCallbackQueue;

    public BleHeartrateMonitor(Context context, PendingCallbackQueue callbackQueue, BluetoothDevice device, Clock clock) {
        super(context, device);
        mHeartrateData = new HeartrateSensorData(clock);
        mCallbackQueue = callbackQueue;
    }

    private final BluetoothGattCallback gattCallback = new BleDevice.BaseBluetoothGattCallback() {
        BluetoothGattCharacteristic mBatteryCharacteristic;

        BluetoothGattCharacteristic mHeartrateCharacteristic;

        boolean mNotificationRequested;

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            AppLog.ble("onServicesDiscovered :: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                // バッテリーステータスもリクエストしておく
                mHeartrateCharacteristic = BluetoothLeUtil.findBluetoothGattCharacteristic(mBleGatt, BLE_UUID_SERVICE_HEARTRATE, BLE_UUID_HEARTRATE_MEASUREMENT);
                mBatteryCharacteristic = BluetoothLeUtil.findBluetoothGattCharacteristic(mBleGatt, BLE_UUID_SERVICE_BATTERY, BLE_UUID_BATTERY_LEVEL);
                if (mBatteryCharacteristic != null) {
                    AppLog.ble("Poll Heartrate Battery Notification");
                    gatt.readCharacteristic(mBatteryCharacteristic);
                } else {
//                    AppLog.ble("Poll Heartrate BPM Notification");
//                    gatt.readCharacteristic(mHeartrateCharacteristic);
                }

//                if (notificationEnable(BLE_UUID_SERVICE_HEARTRATE, BLE_UUID_HEARTRATE_MEASUREMENT)) {
//                    mHeartrateGatt = gatt;
//                    AppLog.ble("Enable Heartrate notification");
//                    mCallbackQueue.run(CallbackTime.Alive, () -> {
//                        for (BleHeartrateListener listener : mListeners) {
//                            listener.onDeviceSupportedHeartrate(BleHeartrateMonitor.this, mDevice);
//                        }
//                    });
//                } else {
//                    mCallbackQueue.run(CallbackTime.Alive, () -> {
//                        for (BleHeartrateListener listener : mListeners) {
//                            listener.onDeviceNotSupportedHeartrate(BleHeartrateMonitor.this, mDevice);
//                        }
//                    });
//                }

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            AppLog.ble("onCharacteristicRead[%s]", characteristic.getUuid().toString());
            if (characteristic.getUuid().equals(BLE_UUID_BATTERY_LEVEL)) {
                int batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                AppLog.ble("HR Monitor Battery[%d]", batteryLevel);
            }

            if (!mNotificationRequested && notificationEnable(BLE_UUID_SERVICE_HEARTRATE, BLE_UUID_HEARTRATE_MEASUREMENT)) {
                mNotificationRequested = true;
                AppLog.ble("Enable Heartrate notification");
                mCallbackQueue.run(CallbackTime.Alive, () -> {
                    for (BleHeartrateListener listener : mListeners) {
                        listener.onDeviceSupportedHeartrate(BleHeartrateMonitor.this, mDevice);
                    }
                });
            }

            // HRを再取得リクエスト
            if (isGattConnected()) {
                mBleGatt.readCharacteristic(mBatteryCharacteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(BLE_UUID_HEARTRATE_MEASUREMENT)) {
                // Blt 0bit目〜1bit目のフラグで値の型を判断する
                int flag = characteristic.getProperties();
                int format;
                if ((flag & 0x1) == 0x01) {
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                    AppLog.bleData("heart rate format UINT16");
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                    AppLog.bleData("heart rate format UINT8");
                }

                final int heartrate = characteristic.getIntValue(format, 1);
                AppLog.bleData("heart rate value(%d)", heartrate);
                // 取得できていれば更新を必ず行うようにする
                mHeartrateData.setHeartrate(heartrate);
                // 心拍更新
                for (BleHeartrateListener listener : mListeners) {
                    listener.onHeartrateUpdated(BleHeartrateMonitor.this, mHeartrateData);
                }
            }
        }

    };

    @Override
    protected BluetoothGattCallback getCallback() {
        return gattCallback;
    }

    /**
     * リスナを登録する
     */
    public void registerHeartrateListener(BleHeartrateListener listener) {
        mListeners.add(listener);
    }

    /**
     * リスナを解放する
     */
    public void unregisterCadenceListener(BleHeartrateListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public synchronized void disconnect() {
        notificationDisable(BLE_UUID_SERVICE_HEARTRATE, BLE_UUID_HEARTRATE_MEASUREMENT);
        super.disconnect();
    }

    /**
     * ハートレート状態を取得する
     */
    public interface BleHeartrateListener {
        /**
         * ハートレートモニターに対応していないデバイスの場合
         */
        void onDeviceNotSupportedHeartrate(BleHeartrateMonitor sensor, BluetoothDevice device);

        /**
         * ハートレートモニターに対応しているデバイスの場合
         */
        void onDeviceSupportedHeartrate(BleHeartrateMonitor sensor, BluetoothDevice device);

        /**
         * ハートレートモニターの値が更新された
         */
        void onHeartrateUpdated(BleHeartrateMonitor sensor, HeartrateSensorData heartrate);
    }
}
