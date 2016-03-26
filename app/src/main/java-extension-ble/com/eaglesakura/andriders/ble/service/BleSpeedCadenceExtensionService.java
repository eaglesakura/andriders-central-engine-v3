package com.eaglesakura.andriders.ble.service;

import com.eaglesakura.andriders.ble.hw.cadence.BleCadenceSpeedSensor;
import com.eaglesakura.andriders.ble.hw.cadence.SpeedCadenceGattReceiver;
import com.eaglesakura.andriders.ble.hw.cadence.SpeedCadenceSensorData;
import com.eaglesakura.andriders.extension.DisplayInformation;
import com.eaglesakura.andriders.extension.ExtensionCategory;
import com.eaglesakura.andriders.extension.ExtensionInformation;
import com.eaglesakura.andriders.extension.ExtensionSession;
import com.eaglesakura.andriders.extension.IExtensionService;
import com.eaglesakura.andriders.extension.data.CentralDataExtension;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.framework.service.BaseService;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

public class BleSpeedCadenceExtensionService extends BaseService implements IExtensionService {


    SpeedCadenceGattReceiver mReceiver;

    Clock mClock = Clock.getRealtimeClock();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.log("onBind(%s)", toString());
        ExtensionSession session = ExtensionSession.onBind(this, intent);
        if (session == null) {
            return null;
        }

        return session.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.log("onUnbind(%s)", toString());
        ExtensionSession.onUnbind(this, intent);
        return super.onUnbind(intent);
    }


    @Override
    public ExtensionInformation getExtensionInformation(ExtensionSession session) {
        ExtensionInformation info = new ExtensionInformation(this, "ble_sc");
        info.setSummary("Bluetooth LE対応センサーから速度とケイデンスを取得します");
        info.setCategory(ExtensionCategory.CATEGORY_SPEED_AND_CADENCE);
        return info;
    }

    @Override
    public List<DisplayInformation> getDisplayInformation(ExtensionSession session) {
        return null;
    }

    @Override
    public void onAceServiceConnected(ExtensionSession session) {
        final CentralDataExtension centralDataExtension = session.getCentralDataExtension();
        String address = centralDataExtension.getGadgetAddress(SensorType.CadenceSensor);
        if (StringUtil.isEmpty(address)) {
            return;
        }
        mReceiver = new SpeedCadenceGattReceiver(this, getSubscriptionController(), mClock);
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
    public void onAceServiceDisconnected(ExtensionSession session) {
        if (mReceiver != null) {
            mReceiver.disconnect();
            mReceiver = null;
        }
    }

    @Override
    public void onEnable(ExtensionSession session) {

    }

    @Override
    public void onDisable(ExtensionSession session) {

    }

    @Override
    public void startSetting(ExtensionSession session) {

    }
}
