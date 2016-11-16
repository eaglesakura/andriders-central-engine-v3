package com.eaglesakura.andriders.model.ble;

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
 * Google Fit用デバイス検索を管理する
 */
public class BleDeviceScanner {

    /**
     * フィットネスデバイスのスキャン時間（秒）
     */
    public static final int SCAN_STOP_TIME_SECS = 60;

    final Context mContext;

    final BleDeviceType mType;

    GoogleApiClient mBleClient;

    boolean connecting = false;

    /**
     * 接続対象のデバイス
     */
    String mTargetFitnessDeviceAddress;

    /**
     * 近隣にあるデバイス
     */
    Set<BleDevice> mNearDevices = new HashSet<>();

    /**
     * 自動で再スキャンを開始する
     */
    boolean mAutoRescan = true;

    BleDeviceScanner(Context context, BleDeviceType device) {
        this.mContext = context;
        this.mType = device;
    }

    public GoogleApiClient getBleClient() {
        return mBleClient;
    }

    private void startScan() {
        StartBleScanRequest request = new StartBleScanRequest.Builder()
                .setBleScanCallback(bleScanCallback)
                .setDataTypes(mType.mDataType)
                .setTimeoutSecs(SCAN_STOP_TIME_SECS)
                .build();

        Fitness.BleApi.startBleScan(mBleClient, request);
        AppLog.ble("start scan(%s)", mType.mDataType.getName());
    }

    public void setTargetFitnessDeviceAddress(String targetFitnessDeviceAddress) {
        this.mTargetFitnessDeviceAddress = targetFitnessDeviceAddress;
    }

    public void setAutoRescan(boolean autoRescan) {
        this.mAutoRescan = autoRescan;
    }

    public void clearDeviceCaches() {
        mNearDevices.clear();
    }

    public void rescan() {
        if (mBleClient != null) {
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
        if (mBleClient != null) {
            return;
        }

        mBleClient = new GoogleApiClient.Builder(mContext)
                .addApi(Fitness.BLE_API)
                .build();
        mBleClient.connect();

        startScan();
        connecting = true;
    }

    /**
     * 切断を行う
     */
    public void disconnect() {
        if (mBleClient == null) {
            return;
        }

        connecting = false;

        Fitness.BleApi.stopBleScan(mBleClient, bleScanCallback);
        mBleClient.disconnect();
        mBleClient = null;
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
        Fitness.BleApi.claimBleDevice(mBleClient, address);
    }

    /**
     * 接続を行う
     */
    public void claim(BleDevice device) {
        AppLog.ble("claim name(%s) address(%s)", device.getName(), device.getAddress());
        Fitness.BleApi.claimBleDevice(mBleClient, device).setResultCallback((status) -> {
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
        Fitness.BleApi.unclaimBleDevice(mBleClient, address);
    }

    /**
     * 切断を行う
     */
    public void unclaim(BleDevice device) {
        AppLog.ble("unclaim name(%s) address(%s)", device.getName(), device.getAddress());
        Fitness.BleApi.unclaimBleDevice(mBleClient, device);
    }

    private BleScanCallback userScanCallback;

    private final BleScanCallback bleScanCallback = new BleScanCallback() {
        @Override
        public void onDeviceFound(BleDevice bleDevice) {

            final int oldDeviceNum = mNearDevices.size();
            mNearDevices.add(bleDevice);
            final int newDeviceNum = mNearDevices.size();

            if (oldDeviceNum == newDeviceNum) {
                return;
            }
            AppLog.ble("onDeviceFound name(%s)", bleDevice.getName());

            // 接続対象が限定されている場合
            if (!StringUtil.isEmpty(mTargetFitnessDeviceAddress)) {
                if (bleDevice.getAddress().equals(mTargetFitnessDeviceAddress)) {
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
            AppLog.ble("restart connect(%s)", mType.getFitnessDataType().getName());
            if (connecting && mAutoRescan) {
                startScan();
            } else {
                if (connecting && userScanCallback != null) {
                    userScanCallback.onScanStopped();
                }
            }
        }
    };
}
