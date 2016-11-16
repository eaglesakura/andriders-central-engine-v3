package com.eaglesakura.andriders.ble.base;

import com.eaglesakura.andriders.ble.BleDevice;
import com.eaglesakura.andriders.ble.BleDevice.BleDeviceListener;
import com.eaglesakura.andriders.model.ble.BleDeviceType;
import com.eaglesakura.android.rx.BackgroundTaskBuilder;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.PendingCallbackQueue;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

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

    @Nullable
    BleDevice mDevice;

    protected String mTargetFitnessDeviceAddress;

    protected final BleDeviceType mFitnessDeviceType;


    /**
     * 再接続待ちの時間
     */
    protected long mReconnectWaitTime = DEFAULT_RECONNECT_DELAY_MS;

    @NonNull
    protected Context mContext;

    @NonNull
    protected final PendingCallbackQueue mCallbackQueue;

    public BaseBleGattReceiver(Context context, PendingCallbackQueue pendingCallbackQueue, BleDeviceType type) {
        mContext = context.getApplicationContext();
        mCallbackQueue = pendingCallbackQueue;
        mFitnessDeviceType = type;
    }

    public void setTargetFitnessDeviceAddress(String targetFitnessDeviceAddress) {
        this.mTargetFitnessDeviceAddress = targetFitnessDeviceAddress;
    }

    @UiThread
    public void connect() {
        AndroidThreadUtil.assertUIThread();

        onSensorScanStart();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        new BackgroundTaskBuilder<BluetoothDevice>(mCallbackQueue)
                .callbackOn(CallbackTime.Alive)
                .executeOn(ExecuteTarget.LocalParallel)
                .async(task -> {
                    BluetoothDevice device = adapter.getRemoteDevice(mTargetFitnessDeviceAddress);
                    if (!adapter.isEnabled() || device == null) {
                        throw new IllegalStateException();
                    } else {
                        return device;
                    }
                })
                .completed((device, task) -> {
                    onSensorFound(device);
                    mDevice = newBleDevice(device);
                    mDevice.registerDeviceListener(mBaseDeviceListener);
                    mDevice.connect();

                    // GATTへの接続タイムアウトチェックする
                    requestGattTimeoutCheck(GATT_CONNECT_TIMEOUT_MS);

                    // 再接続遅延時間をリセットする
                    mReconnectWaitTime = DEFAULT_RECONNECT_DELAY_MS;
                })
                .failed((error, task) -> {
                    requestReScan(DEFAULT_RECONNECT_DELAY_MS);
                    if (!adapter.isEnabled()) {
                        adapter.enable();
                    }
                })
                .start();
    }

    protected abstract BleDevice newBleDevice(BluetoothDevice device);

    /**
     * 切断を行う
     */
    @UiThread
    public void disconnect() {
        AndroidThreadUtil.assertUIThread();

        if (mDevice != null) {
            mDevice.disconnect();
            mDevice = null;
        }

        UIHandler.getInstance().removeCallbacks(mPendingRescanRunner);
        UIHandler.getInstance().removeCallbacks(mConnectionCheckRunner);
    }

    protected abstract void onSensorScanStart();

    protected abstract void onSensorFound(BluetoothDevice device);

    private final BleDeviceListener mBaseDeviceListener = new BleDeviceListener() {
        @Override
        public void onDeviceConnected(BleDevice self, BluetoothDevice device) {
            mCallbackQueue.run(ObserveTarget.Alive, () -> {
                incrementConnectCount(device);
            });
        }

        @Override
        public void onDeviceDisconnected(BleDevice self) {
            // 再度検出を行わせる
            mCallbackQueue.run(ObserveTarget.Alive, () -> {
                disconnect();
                requestReScan(DEFAULT_RECONNECT_DELAY_MS);
            });
        }
    };

    void incrementConnectCount(final BluetoothDevice device) {
        new BackgroundTaskBuilder<Void>(mCallbackQueue)
                .callbackOn(CallbackTime.Foreground)
                .executeOn(ExecuteTarget.LocalParallel)
                .async(task -> {
                    throw new Error("NotImpl");
//                    FitnessDeviceCacheDatabase db = new FitnessDeviceCacheDatabase(mContext);
//                    try {
//                        db.openWritable();
//                        DbBleFitnessDevice fitnessDevice = db.load(device.getAddress());
//                        fitnessDevice.setSelectedCount(fitnessDevice.getSelectedCount() + 1);
//
//                        db.update(fitnessDevice);
//                    } catch (Exception e) {
//
//                    } finally {
//                        db.close();
//                    }
//                    return null;
                })
                .start();
    }

    void requestReScan(long delayTimeMs) {
        UIHandler.rePostDelayedUI(mPendingRescanRunner, delayTimeMs);
    }

    void requestGattTimeoutCheck(long timeoutMs) {
        UIHandler.rePostDelayedUI(mConnectionCheckRunner, timeoutMs);
    }

    protected boolean isConnectedGatt() {
        return mDevice != null && mDevice.isGattConnected();
    }

    /**
     * 再スキャンの開始を行わせるRunner
     * <br>
     * 切断後、ある程度間を置いて再スキャンを行わせる
     */
    private final Runnable mPendingRescanRunner = () -> {
        connect();
    };

    /**
     * コネクションをチェックし、タイムアウトしているようなら再接続を促す
     */
    private final Runnable mConnectionCheckRunner = () -> {
        if (isConnectedGatt()) {
            return;
        }
        // GATT接続に失敗しているようなので、切断して再接続を促す
        disconnect();
        requestReScan(mReconnectWaitTime);

        // 再接続時間を長くする
        mReconnectWaitTime = (int) Math.min(DEFAULT_RECONNECT_BACKOFF * mReconnectWaitTime, MAX_RECONNECT_DELAY_MS);
    };
}
