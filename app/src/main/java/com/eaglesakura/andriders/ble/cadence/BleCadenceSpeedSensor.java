package com.eaglesakura.andriders.ble.cadence;

import com.eaglesakura.andriders.ble.BleDevice;
import com.eaglesakura.android.bluetooth.BluetoothLeUtil;
import com.eaglesakura.android.thread.HandlerThreadExecuter;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.util.Util;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ケイデンスセンサー
 */
public class BleCadenceSpeedSensor extends BleDevice {
    /**
     * スピード・ケイデンスセンサーのBLEデバイスを示すUUID
     * https://developer.bluetooth.org/gatt/services/Pages/ServiceViewer.aspx?u=org.bluetooth.service.cycling_speed_and_cadence.xml
     */
    public static final UUID BLE_UUID_SERVICE_SPEED_AND_CADENCE = BluetoothLeUtil.createUUIDFromAssignedNumber("0x1816");

    /**
     * スピード・ケイデンスセンサーの各種パラメーター取得
     * https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.csc_measurement.xml
     */
    public static final UUID BLE_UUID_SPEEDCADENCE_MEASUREMENT = BluetoothLeUtil.createUUIDFromAssignedNumber("0x2A5B");

    /**
     * リスナ
     */
    private List<BleSpeedCadenceListener> listeners = new ArrayList<BleSpeedCadenceListener>();

    /**
     * ケイデンスデータ
     */
    protected SpeedCadenceData cadence = new SpeedCadenceData();

    /**
     * スピードデータ
     */
    protected SpeedCadenceData speed = new SpeedCadenceData();

    public BleCadenceSpeedSensor(Context context, BluetoothDevice device) {
        super(context, device);

        // スピードセンサーはタイヤの回転数的に頻繁な更新で問題ない
        speed.setStatusCheckIntervalMs(1.5 * 1000.0);
        speed.setSensorTimeoutIntervalMs(3.0 * 1000.0);
    }

    private final BluetoothGattCallback gattCallback = new BleDevice.BaseBluetoothGattCallback() {
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            log("onServicesDiscovered :: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (notificationEnable(BLE_UUID_SERVICE_SPEED_AND_CADENCE, BLE_UUID_SPEEDCADENCE_MEASUREMENT)) {
                    log("enable cadence notification");
                    UIHandler.postUI(new Runnable() {
                        @Override
                        public void run() {
                            for (BleSpeedCadenceListener listener : listeners) {
                                listener.onDeviceSupportedSpeedCadence(BleCadenceSpeedSensor.this, device);
                            }
                        }
                    });
                } else {
                    UIHandler.postUI(new Runnable() {
                        @Override
                        public void run() {
                            for (BleSpeedCadenceListener listener : listeners) {
                                listener.onDeviceNotSupportedSpeedCadence(BleCadenceSpeedSensor.this, device);
                            }
                        }
                    });
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(BLE_UUID_SPEEDCADENCE_MEASUREMENT)) {

                boolean hasCadence;
                boolean hasSpeed;
                {
                    int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    // ビット[0]がホイール回転数
                    hasCadence = (flags & 0x01) != 0;

                    // ビット[1]がクランクケイデンス
                    hasSpeed = (flags & (0x01 << 1)) != 0;

//                    log(String.format("gatt cadence(%s)  speed(%s) bits(%s)", String.valueOf(hasCadence), String.valueOf(hasSpeed), Integer.toBinaryString(flags)));
                }

                int offset = 1;

                HandlerThreadExecuter executer = new HandlerThreadExecuter();
                // スピードセンサーチェック
                if (hasSpeed) {
                    int cumulativeWheelRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset);
                    offset += 4;

                    int lastWheelEventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                    offset += 2;

//                    log(String.format("wheel [%.2f km/h] cumulativeWheelRevolutions(%d)  lastWheelEventTime(%d)", speed.getSpeedKmPerHour(personal.getWheelOuterLength()), cumulativeWheelRevolutions, lastWheelEventTime));
                    // 速度更新
                    speed.update(cumulativeWheelRevolutions, lastWheelEventTime);
//                        log(String.format("wheel %.2f rpm", speed.getRpm()));
                    executer.add(new Runnable() {
                        @Override
                        public void run() {
                            for (BleSpeedCadenceListener listener : listeners) {
                                listener.onSpeedUpdated(BleCadenceSpeedSensor.this, speed);
                            }
                        }
                    });
                }

                // ケイデンスセンサーチェック
                if (hasCadence) {
                    int cumulativeCrankRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                    offset += 2;
                    int lastCrankEventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                    offset += 2;
//                    log(String.format("crank cumulativeCrankRevolutions(%d)  lastCrankEventTime(%d = %f)", cumulativeCrankRevolutions, lastCrankEventTime, ((float) lastCrankEventTime / 1024.0f)));

                    // ケイデンス更新
                    cadence.update(cumulativeCrankRevolutions, lastCrankEventTime);
//                        log(String.format("cadence %d rpm", cadence.getRpmInt()));
                    executer.add(new Runnable() {
                        @Override
                        public void run() {
                            for (BleSpeedCadenceListener listener : listeners) {
                                listener.onCadenceUpdated(BleCadenceSpeedSensor.this, cadence);
                            }
                        }
                    });
                }
                executer.execute();
            }
        }
    };

    @Override
    public void disconnect() {
        notificationDisable(BLE_UUID_SERVICE_SPEED_AND_CADENCE, BLE_UUID_SPEEDCADENCE_MEASUREMENT);
        super.disconnect();
    }

    /**
     * 現在のケイデンスデータを取得する
     */
    public SpeedCadenceData getCadence() {
        return cadence;
    }

    @Override
    protected BluetoothGattCallback getCallback() {
        return gattCallback;
    }

    /**
     * リスナを登録する
     */
    public void registerCadenceListener(BleSpeedCadenceListener listener) {
        Util.addUnique(listeners, listener);
    }

    /**
     * リスナを解放する
     */
    public void unregisterCadenceListener(BleSpeedCadenceListener listener) {
        listeners.remove(listener);
    }

    @Override
    protected void log(String msg) {
        Log.d("BLE-C&S", msg);
    }

    /**
     * ケイデンス状態を取得する
     */
    public interface BleSpeedCadenceListener {
        /**
         * ケイデンスセンサーに対応していないデバイスの場合
         */
        void onDeviceNotSupportedSpeedCadence(BleCadenceSpeedSensor sensor, BluetoothDevice device);

        /**
         * ケイデンスセンサーに対応しているデバイスの場合
         */
        void onDeviceSupportedSpeedCadence(BleCadenceSpeedSensor sensor, BluetoothDevice device);

        /**
         * ケイデンスセンサーの値が更新された
         */
        void onCadenceUpdated(BleCadenceSpeedSensor sensor, SpeedCadenceData cadence);

        /**
         * スピードセンサーの値が更新された
         */
        void onSpeedUpdated(BleCadenceSpeedSensor sensor, SpeedCadenceData speed);
    }
}
