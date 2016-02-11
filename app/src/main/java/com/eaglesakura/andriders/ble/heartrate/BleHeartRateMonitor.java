package com.eaglesakura.andriders.ble.heartrate;

import com.eaglesakura.andriders.ble.BleDevice;
import com.eaglesakura.android.bluetooth.BluetoothLeUtil;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BleHeartRateMonitor extends BleDevice {

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
     * 心拍リスナ
     */
    private final Set<BleHeartrateListener> listeners = new HashSet<>();

    /**
     * 心拍データ
     */
    private HeartrateData heartrateData = new HeartrateData();

    /**
     * 心拍チェック用GATT
     */
    private BluetoothGatt heartrateGatt;

    public BleHeartRateMonitor(Context context, BluetoothDevice device) {
        super(context, device);
    }

    private final BluetoothGattCallback gattCallback = new BleDevice.BaseBluetoothGattCallback() {
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            log("onServicesDiscovered :: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (notificationEnable(BLE_UUID_SERVICE_HEARTRATE, BLE_UUID_HEARTRATE_MEASUREMENT)) {
                    heartrateGatt = gatt;
                    log("enable cadence notification");
                    UIHandler.postUI(new Runnable() {
                        @Override
                        public void run() {
                            for (BleHeartrateListener listener : listeners) {
                                listener.onDeviceSupportedHeartrate(BleHeartRateMonitor.this, device);
                            }
                        }
                    });
                } else {
                    UIHandler.postUI(new Runnable() {
                        @Override
                        public void run() {
                            for (BleHeartrateListener listener : listeners) {
                                listener.onDeviceNotSupportedHeartrate(BleHeartRateMonitor.this, device);
                            }
                        }
                    });
                }
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
//                    log("heart rate format UINT16");
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                    log("heart rate format UINT8");
                }

                int heartrate = characteristic.getIntValue(format, 1);
//                log("heart rate value : " + heartrate);
                // 取得できていれば更新を必ず行うようにする
                heartrateData.update(heartrate);
                // 心拍更新
                for (BleHeartrateListener listener : listeners) {
                    listener.onHeartrateUpdated(BleHeartRateMonitor.this, heartrateData);
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
        listeners.add(listener);
    }

    /**
     * リスナを解放する
     */
    public void unregisterCadenceListener(BleHeartrateListener listener) {
        listeners.remove(listener);
    }

    @Override
    public synchronized void disconnect() {
        notificationDisable(BLE_UUID_SERVICE_HEARTRATE, BLE_UUID_HEARTRATE_MEASUREMENT);
        super.disconnect();
    }

    @Override
    protected void log(String msg) {
        Log.d("BLE-HR", msg);
    }

    /**
     * ハートレート状態を取得する
     */
    public interface BleHeartrateListener {
        /**
         * ハートレートモニターに対応していないデバイスの場合
         */
        void onDeviceNotSupportedHeartrate(BleHeartRateMonitor sensor, BluetoothDevice device);

        /**
         * ハートレートモニターに対応しているデバイスの場合
         */
        void onDeviceSupportedHeartrate(BleHeartRateMonitor sensor, BluetoothDevice device);

        /**
         * ハートレートモニターの値が更新された
         */
        void onHeartrateUpdated(BleHeartRateMonitor sensor, HeartrateData heartrate);
    }
}
