package com.eaglesakura.andriders.ble.service;

import com.eaglesakura.andriders.ble.hw.heartrate.BleHeartrateMonitor;
import com.eaglesakura.andriders.ble.hw.heartrate.HeartrateGattReceiver;
import com.eaglesakura.andriders.ble.hw.heartrate.HeartrateSensorData;
import com.eaglesakura.andriders.plugin.AcePluginService;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.CentralEngineConnection;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.plugin.data.CentralEngineData;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.andriders.service.base.AppBaseService;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.util.StringUtil;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

public class BleHeartrateExtensionService extends AppBaseService implements AcePluginService {
    HeartrateGattReceiver receiver;

    /**
     * リアルタイム同期用時計
     */
    private final Clock mClock = Clock.getRealtimeClock();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AppLog.system("onBind(%s) : %s", intent.getAction(), toString());
        CentralEngineConnection session = CentralEngineConnection.onBind(this, intent);
        if (session == null) {
            return null;
        }
        return session.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AppLog.system("onUnbind(%s) : %s", intent.getAction(), toString());
        CentralEngineConnection.onUnbind(this, intent);
        return super.onUnbind(intent);
    }


    @Override
    public PluginInformation getExtensionInformation(CentralEngineConnection connection) {
        PluginInformation info = new PluginInformation(this, "ble_hr");
        info.setSummary("Bluetooth LE対応センサーから心拍を取得します");
        info.setCategory(Category.CATEGORY_HEARTRATEMONITOR);
        return info;
    }

    @Override
    public List<DisplayKey> getDisplayInformation(CentralEngineConnection connection) {
        return null;
    }

    @Override
    public void onAceServiceConnected(CentralEngineConnection connection) {
        final CentralEngineData centralDataExtension = connection.getCentralDataExtension();
        String address = centralDataExtension.getGadgetAddress(SensorType.HeartrateMonitor);
        if (StringUtil.isEmpty(address)) {
            return;
        }

        receiver = new HeartrateGattReceiver(this, getCallbackQueue(), mClock);
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
                centralDataExtension.setHeartrate(heartrate.getBpm());
            }
        });

        receiver.connect();
    }

    @Override
    public void onAceServiceDisconnected(CentralEngineConnection connection) {
        if (receiver != null) {
            receiver.disconnect();
            receiver = null;
        }
    }

    @Override
    public void onEnable(CentralEngineConnection connection) {

    }

    @Override
    public void onDisable(CentralEngineConnection connection) {

    }

    @Override
    public void startSetting(CentralEngineConnection connection) {

    }

}
