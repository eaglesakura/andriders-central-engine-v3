package com.eaglesakura.andriders.plugin.service;

import com.eaglesakura.andriders.ble.heartrate.BleHeartrateMonitor;
import com.eaglesakura.andriders.ble.heartrate.HeartrateGattReceiver;
import com.eaglesakura.andriders.ble.heartrate.HeartrateSensorData;
import com.eaglesakura.andriders.plugin.AcePluginService;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;
import com.eaglesakura.andriders.plugin.data.CentralEngineSessionData;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.util.StringUtil;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

public class BleHeartratePluginService extends Service implements AcePluginService {
    HeartrateGattReceiver receiver;

    /**
     * リアルタイム同期用時計
     */
    private final Clock mClock = Clock.getRealtimeClock();

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
        info.setSummary("Bluetooth LE対応センサーから心拍を取得します");
        info.setCategory(Category.CATEGORY_HEARTRATEMONITOR);
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
        receiver = new HeartrateGattReceiver(this, mLifecycleDelegate.getCallbackQueue(), mClock);
        receiver.setTargetFitnessDeviceAddress(address);
        receiver.setHeartrateListener(new BleHeartrateMonitor.BleHeartrateListener() {
            @Override
            public void onDeviceNotSupportedHeartrate(BleHeartrateMonitor sensor, BluetoothDevice device) {
            }

            @Override
            public void onDeviceSupportedHeartrate(BleHeartrateMonitor sensor, BluetoothDevice device) {
            }

            @Override
            public void onHeartrateUpdated(BleHeartrateMonitor sensor, HeartrateSensorData heartrate) {
                centralData.setHeartrate(heartrate.getBpm());
            }
        });

        receiver.connect();
    }

    @Override
    public void onAceServiceDisconnected(PluginConnection connection) {
        if (receiver != null) {
            receiver.disconnect();
            receiver = null;
        }


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

    }

}
