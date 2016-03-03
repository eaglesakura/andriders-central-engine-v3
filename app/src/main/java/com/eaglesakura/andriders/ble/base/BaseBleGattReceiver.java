package com.eaglesakura.andriders.ble.base;

import com.eaglesakura.andriders.ble.BleDevice;
import com.eaglesakura.andriders.ble.BleDevice.BleDeviceListener;
import com.eaglesakura.andriders.dao.bledevice.DbBleFitnessDevice;
import com.eaglesakura.andriders.db.fit.FitnessDeviceCacheDatabase;
import com.eaglesakura.andriders.google.FitnessDeviceType;
import com.eaglesakura.android.bluetooth.BluetoothDeviceScanner;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

public abstract class BaseBleGattReceiver {

    static final int DEFAULT_RECONNECT_DELAY_MS = 1000 * 5;

    /**
     * 最大接続待ち遅延時間
     */
    static final int MAX_RECONNECT_DELAY_MS = 1000 * 30;

    static final float DEFAULT_RECONNECT_BACKOFF = 1.2f;

    /**
     * GATTに接続するタイムアウト時間
     */
    static final int GATT_CONNECT_TIMEOUT_MS = 1000 * 30;

    BleDevice device;

    protected String targetFitnessDeviceAddress;

    protected final FitnessDeviceType fitnessDeviceType;

    private BluetoothDeviceScanner deviceScanner;

    /**
     * 再接続待ちの時間
     */
    protected long reconnectWaitTime = DEFAULT_RECONNECT_DELAY_MS;

    protected Context context;


    public BaseBleGattReceiver(Context context, FitnessDeviceType type) {
        this.context = context.getApplicationContext();
        this.fitnessDeviceType = type;
        this.device = null;
    }

    public void setTargetFitnessDeviceAddress(String targetFitnessDeviceAddress) {
        this.targetFitnessDeviceAddress = targetFitnessDeviceAddress;
    }


    public void connect() {
        if (deviceScanner != null) {
            return;
        }
        onSensorScanStart();

        throw new IllegalAccessError("not impl");
//        new AsyncAction("BleConnectiong") {
//            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//
//            @Override
//            protected Object onBackgroundAction() throws Exception {
//                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(targetFitnessDeviceAddress);
//                if (!adapter.isEnabled() || device == null) {
//                    throw new IllegalStateException();
//                } else {
//                    return device;
//                }
//            }
//
//            @Override
//            protected void onSuccess(Object object) {
//                BluetoothDevice device = (BluetoothDevice) object;
//                onSensorFound(device);
//                BaseBleGattReceiver.this.device = newBleDevice(device);
//                BaseBleGattReceiver.this.device.registerDeviceListener(baseDeviceListener);
//                BaseBleGattReceiver.this.device.connect();
//
//                // GATTへの接続タイムアウトチェックする
//                requestGattTimeoutCheck(GATT_CONNECT_TIMEOUT_MS);
//
//                // 再接続遅延時間をリセットする
//                reconnectWaitTime = DEFAULT_RECONNECT_DELAY_MS;
//            }
//
//            @Override
//            protected void onFailure(Exception exception) {
//                requestReScan(DEFAULT_RECONNECT_DELAY_MS);
//                if (!adapter.isEnabled()) {
//                    adapter.enable();
//                }
//            }
//        }.start();
    }

    protected abstract BleDevice newBleDevice(BluetoothDevice device);

    public void disconnect() {
        if (deviceScanner != null) {
            deviceScanner.stopScan();
            deviceScanner = null;
        }

        if (device != null) {
            device.disconnect();
            device = null;
        }

        UIHandler.getInstance().removeCallbacks(pendingRescanRunner);
        UIHandler.getInstance().removeCallbacks(connectionCheckRunner);
    }

    protected abstract void onSensorScanStart();

    protected abstract void onSensorFound(BluetoothDevice device);

    private final BleDeviceListener baseDeviceListener = new BleDeviceListener() {
        @Override
        public void onDeviceConnected(BleDevice self, BluetoothDevice device) {
            incrementConnectCount(device);
        }

        @Override
        public void onDeviceDisconnected(BleDevice self) {
            // 再度検出を行わせる
            disconnect();
            requestReScan(DEFAULT_RECONNECT_DELAY_MS);
        }
    };

    void incrementConnectCount(final BluetoothDevice device) {
        throw new IllegalAccessError("not impl");
//        FrameworkCentral.getTaskController().pushBack(new Runnable() {
//            @Override
//            public void run() {
//                FitnessDeviceCacheDatabase db = new FitnessDeviceCacheDatabase(context);
//                try {
//                    db.openWritable();
//                    DbBleFitnessDevice fitnessDevice = db.load(device.getAddress());
//                    fitnessDevice.setSelectedCount(fitnessDevice.getSelectedCount() + 1);
//
//                    db.update(fitnessDevice);
//                } catch (Exception e) {
//
//                } finally {
//                    db.close();
//                }
//            }
//        });
    }

    void requestReScan(long delayTimeMs) {
        UIHandler.getInstance().removeCallbacks(pendingRescanRunner);
        UIHandler.postDelayedUI(pendingRescanRunner, delayTimeMs);
    }

    void requestGattTimeoutCheck(long timeoutMs) {
        UIHandler.getInstance().removeCallbacks(connectionCheckRunner);
        UIHandler.postDelayedUI(connectionCheckRunner, timeoutMs);
    }

    protected boolean isConnectedGatt() {
        return device != null && device.isGattConnected();
    }

    /**
     * 再スキャンの開始を行わせるRunner
     * <br>
     * 切断後、ある程度間を置いて再スキャンを行わせる
     */
    final Runnable pendingRescanRunner = new Runnable() {
        @Override
        public void run() {
            connect();
        }
    };

    final Runnable connectionCheckRunner = new Runnable() {
        @Override
        public void run() {
            if (isConnectedGatt()) {
                return;
            }
            // GATT接続に失敗しているようなので、切断して再接続を促す
            disconnect();
            requestReScan(reconnectWaitTime);

            // 再接続時間を長くする
            reconnectWaitTime = (int) Math.min(DEFAULT_RECONNECT_BACKOFF * reconnectWaitTime, MAX_RECONNECT_DELAY_MS);
        }
    };
}
