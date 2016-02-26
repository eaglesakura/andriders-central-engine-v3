package com.eaglesakura.andriders.extension;

import com.eaglesakura.andriders.ble.cadence.BleCadenceSpeedSensor;
import com.eaglesakura.andriders.ble.cadence.SpeedCadenceData;
import com.eaglesakura.andriders.ble.cadence.SpeedCadenceGattReceiver;
import com.eaglesakura.andriders.extension.data.CentralDataExtension;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.android.framework.service.BaseService;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

public class BleSpeedCadenceExtensionService extends BaseService implements IExtensionService {


    SpeedCadenceGattReceiver receiver;

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
        receiver = new SpeedCadenceGattReceiver(this);
        receiver.setTargetFitnessDeviceAddress(address);
        receiver.setSpeedCadenceListener(new BleCadenceSpeedSensor.BleSpeedCadenceListener() {
            @Override
            public void onDeviceNotSupportedSpeedCadence(BleCadenceSpeedSensor sensor, BluetoothDevice device) {
            }

            @Override
            public void onDeviceSupportedSpeedCadence(BleCadenceSpeedSensor sensor, BluetoothDevice device) {

            }

            @Override
            public void onCadenceUpdated(BleCadenceSpeedSensor sensor, SpeedCadenceData cadence) {
                centralDataExtension.setSpeedAndCadence((float) cadence.getRpm(), cadence.getSumRevolutions(), -1, -1);
            }

            @Override
            public void onSpeedUpdated(BleCadenceSpeedSensor sensor, SpeedCadenceData speed) {
                centralDataExtension.setSpeedAndCadence(-1, -1, (float) speed.getRpm(), speed.getSumRevolutions());
            }
        });
        receiver.connect();
    }

    @Override
    public void onAceServiceDisconnected(ExtensionSession session) {
        if (receiver != null) {
            receiver.disconnect();
            receiver = null;
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
