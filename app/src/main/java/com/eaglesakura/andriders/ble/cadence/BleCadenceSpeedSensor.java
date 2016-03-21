package com.eaglesakura.andriders.ble.cadence;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.ble.BleDevice;
import com.eaglesakura.andriders.central.Clock;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.bluetooth.BluetoothLeUtil;
import com.eaglesakura.android.thread.HandlerThreadExecuter;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.util.CollectionUtil;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

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
    private List<BleSpeedCadenceListener> mListeners = new ArrayList<>();

    /**
     * ケイデンスデータ
     */
    protected SpeedCadenceSensorData mCadence;

    /**
     * スピードデータ
     */
    protected SpeedCadenceSensorData mSpeed;

    public BleCadenceSpeedSensor(Context context, BluetoothDevice device, Clock clock) {
        super(context, device);

        // スピードセンサーはタイヤの回転数的に頻繁な更新で問題ない
        mCadence = new SpeedCadenceSensorData(clock, 1000 * 2, BleDevice.SENSOR_TIMEOUT_MS);
        mSpeed = new SpeedCadenceSensorData(clock, (int) (1.5 * 1000), 1000 * 3);
    }

    /**
     * ホイールの外周を取得する
     */
    double getWheelOuterLength() {
        return Settings.getInstance().getUserProfiles().getWheelOuterLength();
    }

    private final BluetoothGattCallback gattCallback = new BleDevice.BaseBluetoothGattCallback() {
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            AppLog.ble("onServicesDiscovered :: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (notificationEnable(BLE_UUID_SERVICE_SPEED_AND_CADENCE, BLE_UUID_SPEEDCADENCE_MEASUREMENT)) {
                    AppLog.ble("enable cadence notification");
                    UIHandler.postUI(new Runnable() {
                        @Override
                        public void run() {
                            for (BleSpeedCadenceListener listener : mListeners) {
                                listener.onDeviceSupportedSpeedCadence(BleCadenceSpeedSensor.this, mDevice);
                            }
                        }
                    });
                } else {
                    UIHandler.postUI(new Runnable() {
                        @Override
                        public void run() {
                            for (BleSpeedCadenceListener listener : mListeners) {
                                listener.onDeviceNotSupportedSpeedCadence(BleCadenceSpeedSensor.this, mDevice);
                            }
                        }
                    });
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(BLE_UUID_SPEEDCADENCE_MEASUREMENT)) {

                final boolean hasCadence;
                final boolean hasSpeed;
                {
                    int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    // ビット[0]がホイール回転数
                    hasCadence = (flags & 0x01) != 0;

                    // ビット[1]がクランクケイデンス
                    hasSpeed = (flags & (0x01 << 1)) != 0;

                    AppLog.bleData(String.format("gatt cadence(%s)  speed(%s) bits(%s)", String.valueOf(hasCadence), String.valueOf(hasSpeed), Integer.toBinaryString(flags)));
                }

                int offset = 1;

                HandlerThreadExecuter executer = new HandlerThreadExecuter();
                // スピードセンサーチェック
                if (hasSpeed) {
                    int cumulativeWheelRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset);
                    offset += 4;

                    int lastWheelEventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                    offset += 2;

                    AppLog.bleData(String.format("wheel [%.2f km/h] cumulativeWheelRevolutions(%d)  lastWheelEventTime(%d)",
                            AceUtils.calcSpeedKmPerHour(mSpeed.getRpm(), getWheelOuterLength()), cumulativeWheelRevolutions, lastWheelEventTime));
                    // 速度更新
                    mSpeed.update(cumulativeWheelRevolutions, lastWheelEventTime);
                    AppLog.bleData(String.format("wheel %.2f rpm", mSpeed.getRpm()));
                    executer.add(new Runnable() {
                        @Override
                        public void run() {
                            for (BleSpeedCadenceListener listener : mListeners) {
                                listener.onSpeedUpdated(BleCadenceSpeedSensor.this, mSpeed);
                            }
                        }
                    });
                }

                // ケイデンスセンサーチェック
                if (hasCadence) {
                    final int cumulativeCrankRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                    offset += 2;
                    final int lastCrankEventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                    offset += 2;
                    AppLog.bleData(String.format("crank cumulativeCrankRevolutions(%d)  lastCrankEventTime(%d = %f)", cumulativeCrankRevolutions, lastCrankEventTime, ((float) lastCrankEventTime / 1024.0f)));

                    // ケイデンス更新
                    mCadence.update(cumulativeCrankRevolutions, lastCrankEventTime);
                    AppLog.bleData(String.format("cadence %d rpm", (int) mCadence.getRpm()));
                    executer.add(new Runnable() {
                        @Override
                        public void run() {
                            for (BleSpeedCadenceListener listener : mListeners) {
                                listener.onCadenceUpdated(BleCadenceSpeedSensor.this, mCadence);
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
    public SpeedCadenceSensorData getCadence() {
        return mCadence;
    }

    @Override
    protected BluetoothGattCallback getCallback() {
        return gattCallback;
    }

    /**
     * リスナを登録する
     */
    public void registerCadenceListener(BleSpeedCadenceListener listener) {
        CollectionUtil.addUnique(mListeners, listener);
    }

    /**
     * リスナを解放する
     */
    public void unregisterCadenceListener(BleSpeedCadenceListener listener) {
        mListeners.remove(listener);
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
        void onCadenceUpdated(BleCadenceSpeedSensor sensor, SpeedCadenceSensorData cadence);

        /**
         * スピードセンサーの値が更新された
         */
        void onSpeedUpdated(BleCadenceSpeedSensor sensor, SpeedCadenceSensorData speed);
    }
}
