package com.eaglesakura.andriders.extension;

import com.eaglesakura.andriders.ble.heartrate.BleHeartRateMonitor;
import com.eaglesakura.andriders.ble.heartrate.HeartrateData;
import com.eaglesakura.andriders.ble.heartrate.HeartrateGattReceiver;
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

public class BleHeartrateExtensionService extends BaseService implements IExtensionService {
    HeartrateGattReceiver receiver;

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
        ExtensionInformation info = new ExtensionInformation(this, "ble_hr");
        info.setSummary("Bluetooth LE対応センサーから心拍を取得します");
        info.setCategory(ExtensionCategory.CATEGORY_HEARTRATEMONITOR);
        return info;
    }

    @Override
    public List<DisplayInformation> getDisplayInformation(ExtensionSession session) {
        return null;
    }

    @Override
    public void onAceServiceConnected(ExtensionSession session) {
        final CentralDataExtension centralDataExtension = session.getCentralDataExtension();
        String address = centralDataExtension.getGadgetAddress(SensorType.HeartrateMonitor);
        if (StringUtil.isEmpty(address)) {
            return;
        }

        receiver = new HeartrateGattReceiver(this);
        receiver.setTargetFitnessDeviceAddress(address);
        receiver.setHeartrateListener(new BleHeartRateMonitor.BleHeartrateListener() {
            @Override
            public void onDeviceNotSupportedHeartrate(BleHeartRateMonitor sensor, BluetoothDevice device) {
            }

            @Override
            public void onDeviceSupportedHeartrate(BleHeartRateMonitor sensor, BluetoothDevice device) {
            }

            @Override
            public void onHeartrateUpdated(BleHeartRateMonitor sensor, HeartrateData heartrate) {
                centralDataExtension.setHeartrate(heartrate.getBpm());
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
