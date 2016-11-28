package com.eaglesakura.andriders.ble.service;

import com.eaglesakura.andriders.ble.cadence.BleCadenceSpeedSensor;
import com.eaglesakura.andriders.ble.cadence.SpeedCadenceGattReceiver;
import com.eaglesakura.andriders.ble.cadence.SpeedCadenceSensorData;
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

public class BleSpeedCadenceExtensionService extends AppBaseService implements AcePluginService {


    SpeedCadenceGattReceiver mReceiver;

    Clock mClock = Clock.getRealtimeClock();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AppLog.system("onBind(%s)", toString());
        CentralEngineConnection session = CentralEngineConnection.onBind(this, intent);
        if (session == null) {
            return null;
        }

        return session.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AppLog.system("onUnbind(%s)", toString());
        CentralEngineConnection.onUnbind(this, intent);
        return super.onUnbind(intent);
    }

    @Override
    public PluginInformation getExtensionInformation(CentralEngineConnection connection) {
        PluginInformation info = new PluginInformation(this, "ble_sc");
        info.setSummary("Bluetooth LE対応センサーから速度とケイデンスを取得します");
        info.setCategory(Category.CATEGORY_SPEED_AND_CADENCE);
        return info;
    }

    @Override
    public List<DisplayKey> getDisplayInformation(CentralEngineConnection connection) {
        return null;
    }

    @Override
    public void onAceServiceConnected(CentralEngineConnection connection) {
        final CentralEngineData centralDataExtension = connection.getCentralDataExtension();
        String address = centralDataExtension.getGadgetAddress(SensorType.CadenceSensor);
        if (StringUtil.isEmpty(address)) {
            return;
        }
        mReceiver = new SpeedCadenceGattReceiver(this, getCallbackQueue(), mClock);
        mReceiver.setTargetFitnessDeviceAddress(address);
        mReceiver.setSpeedCadenceListener(new BleCadenceSpeedSensor.BleSpeedCadenceListener() {
            @Override
            public void onDeviceNotSupportedSpeedCadence(BleCadenceSpeedSensor sensor, BluetoothDevice device) {
            }

            @Override
            public void onDeviceSupportedSpeedCadence(BleCadenceSpeedSensor sensor, BluetoothDevice device) {

            }

            @Override
            public void onCadenceUpdated(BleCadenceSpeedSensor sensor, SpeedCadenceSensorData cadence) {
                centralDataExtension.setSpeedAndCadence((float) cadence.getRpm(), cadence.getSumRevolveCount(), -1, -1);
            }

            @Override
            public void onSpeedUpdated(BleCadenceSpeedSensor sensor, SpeedCadenceSensorData speed) {
                centralDataExtension.setSpeedAndCadence(-1, -1, (float) speed.getRpm(), speed.getSumRevolveCount());
            }
        });
        mReceiver.connect();
    }

    @Override
    public void onAceServiceDisconnected(CentralEngineConnection connection) {
        if (mReceiver != null) {
            mReceiver.disconnect();
            mReceiver = null;
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
