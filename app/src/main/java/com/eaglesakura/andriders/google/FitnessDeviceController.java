package com.eaglesakura.andriders.google;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.BleDevice;
import com.google.android.gms.fitness.request.BleScanCallback;
import com.google.android.gms.fitness.request.StartBleScanRequest;

import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.util.StringUtil;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * Google Fit用デバイスを管理する
 */
public class FitnessDeviceController {

    /**
     * フィットネスデバイスのスキャン時間（秒）
     */
    public static final int SCAN_STOP_TIME_SECS = 60;

    final Context context;

    final FitnessDeviceType type;

    GoogleApiClient bleClient;

    boolean connecting = false;

    /**
     * 接続対象のデバイス
     */
    String targetFitnessDeviceAddress;

    /**
     * 近隣にあるデバイス
     */
    Set<BleDevice> nearDevices = new HashSet<>();

    /**
     * 自動で再スキャンを開始する
     */
    boolean autoRescan = true;

    FitnessDeviceController(Context context, FitnessDeviceType device) {
        this.context = context;
        this.type = device;
    }

    public GoogleApiClient getBleClient() {
        return bleClient;
    }

    private void startScan() {
        StartBleScanRequest request = new StartBleScanRequest.Builder()
                .setBleScanCallback(bleScanCallback)
                .setDataTypes(type.dataType)
                .setTimeoutSecs(SCAN_STOP_TIME_SECS)
                .build();

        Fitness.BleApi.startBleScan(bleClient, request);
        AppLog.ble("start scan(%s)", type.dataType.getName());
    }

    public void setTargetFitnessDeviceAddress(String targetFitnessDeviceAddress) {
        this.targetFitnessDeviceAddress = targetFitnessDeviceAddress;
    }

    public void setAutoRescan(boolean autoRescan) {
        this.autoRescan = autoRescan;
    }

    public void clearDeviceCaches() {
        nearDevices.clear();
    }

    public void rescan() {
        if (bleClient != null) {
            connect();
        } else {
            startScan();
            connecting = true;
        }
    }

    /**
     * 接続を行う
     */
    public void connect() {
        if (bleClient != null) {
            return;
        }

        bleClient = new GoogleApiClient.Builder(context)
                .addApi(Fitness.BLE_API)
                .build();
        bleClient.connect();

        startScan();
        connecting = true;
    }

    /**
     * 切断を行う
     */
    public void disconnect() {
        if (bleClient == null) {
            return;
        }

        connecting = false;

        Fitness.BleApi.stopBleScan(bleClient, bleScanCallback);
        bleClient.disconnect();
        bleClient = null;
    }

    /**
     * ユーザー用のコールバックを指定する
     */
    public void setBleScanCallback(BleScanCallback userScanCallback) {
        this.userScanCallback = userScanCallback;
    }

    public void claim(String address) {
        if (StringUtil.isEmpty(address)) {
            return;
        }

        AppLog.ble("claim address(%s)", address);
        Fitness.BleApi.claimBleDevice(bleClient, address);
    }

    /**
     * 接続を行う
     */
    public void claim(BleDevice device) {
        AppLog.ble("claim name(%s) address(%s)", device.getName(), device.getAddress());
        Fitness.BleApi.claimBleDevice(bleClient, device).setResultCallback((status) -> {
            AppLog.ble("claim :: " + (status.isSuccess() ? "success" : "failed"));
        });
    }

    /**
     * 切断を行う
     */
    public void unclaim(String address) {
        if (StringUtil.isEmpty(address)) {
            return;
        }
        AppLog.ble("unclaim address(%s)", address);
        Fitness.BleApi.unclaimBleDevice(bleClient, address);
    }

    /**
     * 切断を行う
     */
    public void unclaim(BleDevice device) {
        AppLog.ble("unclaim name(%s) address(%s)", device.getName(), device.getAddress());
        Fitness.BleApi.unclaimBleDevice(bleClient, device);
    }

    private BleScanCallback userScanCallback;

    private final BleScanCallback bleScanCallback = new BleScanCallback() {
        @Override
        public void onDeviceFound(BleDevice bleDevice) {

            final int oldDeviceNum = nearDevices.size();
            nearDevices.add(bleDevice);
            final int newDeviceNum = nearDevices.size();

            if (oldDeviceNum == newDeviceNum) {
                return;
            }
            AppLog.ble("onDeviceFound name(%s)", bleDevice.getName());

            // 接続対象が限定されている場合
            if (!StringUtil.isEmpty(targetFitnessDeviceAddress)) {
                if (bleDevice.getAddress().equals(targetFitnessDeviceAddress)) {
                    claim(bleDevice);
                } else {
                    // 選択されていないデバイスのため、デバイスを切断してサヨウナラ
                    unclaim(bleDevice);
                    return;
                }
            }

            if (userScanCallback != null) {
                userScanCallback.onDeviceFound(bleDevice);
            }
        }

        @Override
        public void onScanStopped() {
            AppLog.ble("restart connect(%s)", type.getFitnessDataType().getName());
            if (connecting && autoRescan) {
                startScan();
            } else {
                if (connecting && userScanCallback != null) {
                    userScanCallback.onScanStopped();
                }
            }
        }
    };
}
