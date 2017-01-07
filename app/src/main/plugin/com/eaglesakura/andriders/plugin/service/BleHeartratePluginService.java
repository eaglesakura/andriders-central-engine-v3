package com.eaglesakura.andriders.plugin.service;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.plugin.AcePluginService;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;
import com.eaglesakura.andriders.plugin.data.CentralEngineSessionData;
import com.eaglesakura.andriders.plugin.display.DisplayDataSender;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.andriders.ui.navigation.SensorDeviceSettingActivity;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.bluetooth.error.BluetoothException;
import com.eaglesakura.android.bluetooth.gatt.BleDeviceConnection;
import com.eaglesakura.android.bluetooth.gatt.BleGattController;
import com.eaglesakura.android.bluetooth.gatt.BleHeartrateMonitorCallback;
import com.eaglesakura.android.bluetooth.gatt.BlePeripheralDeviceConnection;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.StringUtil;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

public class BleHeartratePluginService extends Service implements AcePluginService {
    ServiceLifecycleDelegate mLifecycleDelegate = new ServiceLifecycleDelegate();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AppLog.system("onBind(%s)", toString());
        PluginConnection session = PluginConnection.onBind(this, intent);
        if (session == null) {
            return null;
        }

        return session.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AppLog.system("onUnbind(%s)", toString());
        PluginConnection.onUnbind(this, intent);
        return super.onUnbind(intent);
    }

    @Override
    public PluginInformation getExtensionInformation(PluginConnection connection) {
        PluginInformation info = new PluginInformation(this, "ble_hr");
        info.setSummary("Bluetooth LE対応センサーから心拍を取得します。");
        info.setCategory(Category.CATEGORY_HEARTRATEMONITOR);
        info.setHasSetting(true);
        return info;
    }

    @Override
    public List<DisplayKey> getDisplayInformation(PluginConnection connection) {
        return null;
    }

    @Override
    public void onAceServiceConnected(PluginConnection connection) {
        final CentralEngineSessionData centralData = connection.getCentralData();
        String address = centralData.getGadgetAddress(SensorType.HeartrateMonitor);
        AppLog.ble("BLE HeartrateSensor[%s]", address);
        if (StringUtil.isEmpty(address)) {
            return;
        }

        mLifecycleDelegate.onCreate();
        mLifecycleDelegate.async(ExecuteTarget.LocalQueue, CallbackTime.Alive, task -> {
            CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task);
            deviceConnectLoop(address, centralData, connection.getDisplay(), cancelCallback);
            return this;
        }).start();
    }

    @Override
    public void onAceServiceDisconnected(PluginConnection connection) {
        mLifecycleDelegate.onDestroy();
    }

    @Override
    public void onEnable(PluginConnection connection) {

    }

    @Override
    public void onDisable(PluginConnection connection) {

    }

    @Override
    public void startSetting(PluginConnection connection) {
        Intent intent = SensorDeviceSettingActivity.Builder.from(this)
                .build();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * BLEデバイス情報取得ループを行う
     *
     * @param address        有効なBLEデバイスアドレス
     * @param centralData    データ送信先
     * @param display        通知送信先
     * @param cancelCallback デフォルトのキャンセルチェック
     */
    private void deviceConnectLoop(String address, CentralEngineSessionData centralData, DisplayDataSender display, CancelCallback cancelCallback) throws Throwable {
        BlePeripheralDeviceConnection.SessionCallback sessionCallback = new BlePeripheralDeviceConnection.SessionCallback() {
            @Override
            public void onSessionStart(BlePeripheralDeviceConnection self, BlePeripheralDeviceConnection.Session session) {
                NotificationData notification = new NotificationData.Builder(getApplication(), NotificationData.ID_GADGET_BLE_HRMONITOR_SEARCH)
                        .message(R.string.Message_Plugin_BleHeartrate_Search)
                        .icon(R.mipmap.ic_launcher)
                        .getNotification();
                display.queueNotification(notification);
            }

            @Override
            public void onSessionFinished(BlePeripheralDeviceConnection self, BlePeripheralDeviceConnection.Session session) {
                if (session.isGattConnected()) {
                    NotificationData notification = new NotificationData.Builder(getApplication(), NotificationData.ID_GADGET_BLE_HRMONITOR_DISCONNECT)
                            .message(R.string.Message_Plugin_BleHeartrate_Disconnected)
                            .icon(R.mipmap.ic_launcher)
                            .getNotification();
                    display.queueNotification(notification);
                }
            }
        };
        BleDeviceConnection.Callback dataCallback = new BleHeartrateMonitorCallback() {

            boolean mBatteryUpdated = false;

            @Override
            public void onGattConnected(BleDeviceConnection self, BleGattController gatt) throws BluetoothException {
                super.onGattConnected(self, gatt);
                NotificationData notification = new NotificationData.Builder(getApplication(), NotificationData.ID_GADGET_BLE_HRMONITOR_CONNECT)
                        .message(R.string.Message_Plugin_BleHeartrate_Connected)
                        .icon(R.mipmap.ic_launcher)
                        .getNotification();
                display.queueNotification(notification);
            }

            /**
             * ループは常に抜けない
             */
            @Override
            public boolean onLoop(BleDeviceConnection self, BleGattController gatt) throws BluetoothException {
                return false;
            }

            @Override
            protected void onUpdateHeartrateBpm(int newBpm) {
                if (newBpm < 50) {
                    // 心拍値に明らかな異常があるのでdropする
                    return;
                }
                centralData.setHeartrate(newBpm);
            }

            @Override
            protected void onUpdateBatteryLevel(int newLevel) {
                // バッテリー残量通知を行う
                if (!mBatteryUpdated) {
                    NotificationData notification = new NotificationData.Builder(getApplication(), NotificationData.ID_GADGET_BLE_HRMONITOR_BATTERY)
                            .message(getString(R.string.Message_Plugin_BleHeartrate_Battery, newLevel))
                            .icon(R.mipmap.ic_launcher)
                            .getNotification();
                    display.queueNotification(notification);
                }
                mBatteryUpdated = true;
            }
        };

        BlePeripheralDeviceConnection deviceConnection =
                new BlePeripheralDeviceConnection(this, address);
        deviceConnection.alwaysConnect(sessionCallback, dataCallback, cancelCallback);
    }
}
