package com.eaglesakura.andriders.ui.navigation.sensor;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.gen.prop.UserProfiles;
import com.eaglesakura.andriders.model.ble.BleDeviceType;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.ui.widget.AppKeyValueView;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.bluetooth.error.BluetoothException;
import com.eaglesakura.android.bluetooth.gatt.BleDeviceConnection;
import com.eaglesakura.android.bluetooth.gatt.BleGattController;
import com.eaglesakura.android.bluetooth.gatt.BleHeartrateMonitorCallback;
import com.eaglesakura.android.bluetooth.gatt.BlePeripheralDeviceConnection;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;
import com.eaglesakura.util.StringUtil;
import com.eaglesakura.util.Util;

import android.app.Dialog;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Date;

/**
 * BLE心拍計設定と接続テストを行なう
 */
@FragmentLayout(R.layout.sensor_gadgets_ble_fitness)
public class BleHeartrateSettingFragment extends BleFitnessSensorSettingFragment {
    public BleHeartrateSettingFragment() {
        initialize(BleDeviceType.HEARTRATE_MONITOR,
                R.drawable.ic_heart_beats, R.string.Word_Gadget_BleHeartrateMonitor,
                UserProfiles.ID_BLEHEARTRATEMONITORADDRESS
        );
    }

    /**
     * 機器への接続テストを行なう
     */
    @OnClick(R.id.Button_Testing)
    void clickTesting() {
        String BLE_ADDRESS = mAppSettings.getUserProfiles().getBleHeartrateMonitorAddress();
        if (StringUtil.isEmpty(BLE_ADDRESS)) {
            AppDialogBuilder.newAlert(getContext(), R.string.Message_Sensor_BleDeviceNotSelected)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getFragmentLifecycle());
            return;
        }

        View content = LayoutInflater.from(getContext()).inflate(R.layout.sensor_gadgets_ble_heartrate_testing, null, false);
        Dialog dialog = AppDialogBuilder.newCustomContent(getContext(), getString(R.string.Word_Sensor_Testing), content)
                .positiveButton(R.string.EsMaterial_Dialog_Close, null)
                .show(getFragmentLifecycle());
        asyncQueue(task -> {
            CancelCallback cancelCallback = SupportCancelCallbackBuilder.from(task).build();
            BlePeripheralDeviceConnection connection = new BlePeripheralDeviceConnection(getContext(), BLE_ADDRESS);
            connection.alwaysConnect(new BlePeripheralDeviceConnection.SessionCallback() {
                @Override
                public void onSessionStart(BlePeripheralDeviceConnection self, BlePeripheralDeviceConnection.Session session) {
                    updateDeviceValue(content, R.string.Word_Sensor_StateConnecting, -1, -1);
                }

                @Override
                public void onSessionFinished(BlePeripheralDeviceConnection self, BlePeripheralDeviceConnection.Session session) {
                    updateDeviceValue(content, R.string.Word_Sensor_StateConnecting, -1, -1);
                }
            }, new BleHeartrateMonitorCallback() {
                @Override
                public boolean onLoop(BleDeviceConnection self, BleGattController gatt) throws BluetoothException {
                    return false;
                }

                @Override
                protected void onUpdateBatteryLevel(int newLevel) {
                    updateDeviceValue(content, R.string.Word_Sensor_StateConnect, newLevel, Util.getInt(getHeartrateBpm(), -1));
                }

                @Override
                protected void onUpdateHeartrateBpm(int newBpm) {
                    updateDeviceValue(content, R.string.Word_Sensor_StateConnect, Util.getInt(getBatteryLevel(), -1), newBpm);
                }
            }, cancelCallback);
            return this;
        }).failed((error, task) -> {
            AppLog.report(error);
        }).cancelSignal(dialog)
                .start();
    }

    /**
     * ダイアログの表示を更新する
     */
    void updateDeviceValue(View content, @StringRes int statusText, int battery, int heartrate) {
        String BATTERY_TEXT = battery > 0 ? StringUtil.format("%d %%", battery) : getString(R.string.Word_Display_NoData);
        String HEARTRATE_TEXT = heartrate > 0 ? StringUtil.format("%d bpm", heartrate) : getString(R.string.Word_Display_NoData);
        UIHandler.postUIorRun(() -> {
            new AQuery(content)
                    .id(R.id.Item_Status).ifPresent(AppKeyValueView.class, view -> view.setValueText(getString(statusText)))
                    .id(R.id.Item_Updated).ifPresent(AppKeyValueView.class, view -> view.setValueText(TIME_FORMATTER.format(new Date())))
                    .id(R.id.Item_Battery).ifPresent(AppKeyValueView.class, view -> view.setValueText(BATTERY_TEXT))
                    .id(R.id.Item_Heartrate).ifPresent(AppKeyValueView.class, view -> view.setValueText(HEARTRATE_TEXT));
        });
    }
}
