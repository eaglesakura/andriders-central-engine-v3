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
import com.eaglesakura.android.bluetooth.gatt.BlePeripheralDeviceConnection;
import com.eaglesakura.android.bluetooth.gatt.BleSpeedCadenceSensorCallback;
import com.eaglesakura.android.bluetooth.gatt.scs.RawSensorValue;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.support.SupportCancelCallbackBuilder;
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
public class BleSpeedCadenceSettingFragment extends BleFitnessSensorSettingFragment {
    public BleSpeedCadenceSettingFragment() {
        initialize(BleDeviceType.SPEED_CADENCE_SENSOR,
                R.drawable.ic_speed, R.string.Word_Gadget_BleSpeedAndCadenceSensor,
                UserProfiles.ID_BLESPEEDCADENCESENSORADDRESS
        );
    }

    /**
     * 機器への接続テストを行なう
     */
    @OnClick(R.id.Button_Testing)
    void clickTesting() {
        String BLE_ADDRESS = mAppSettings.getUserProfiles().getBleSpeedCadenceSensorAddress();
        int WHEEL_LENGTH = mAppSettings.getUserProfiles().getWheelOuterLength();
        if (StringUtil.isEmpty(BLE_ADDRESS)) {
            AppDialogBuilder.newAlert(getContext(), R.string.Message_Sensor_BleDeviceNotSelected)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(mLifecycleDelegate);
            return;
        }

        View content = LayoutInflater.from(getContext()).inflate(R.layout.sensor_gadgets_ble_cadence_testing, null, false);
        Dialog dialog = AppDialogBuilder.newCustomContent(getContext(), getString(R.string.Word_Sensor_Testing), content)
                .positiveButton(R.string.EsMaterial_Dialog_Close, null)
                .show(mLifecycleDelegate);
        asyncUI(task -> {
            CancelCallback cancelCallback = SupportCancelCallbackBuilder.from(task).build();
            BlePeripheralDeviceConnection connection = new BlePeripheralDeviceConnection(getContext(), BLE_ADDRESS);
            connection.alwaysConnect(new BlePeripheralDeviceConnection.SessionCallback() {
                @Override
                public void onSessionStart(BlePeripheralDeviceConnection self, BlePeripheralDeviceConnection.Session session) {
                    updateDeviceValue(content, R.string.Word_Sensor_StateConnecting, -1, -1, -1);
                }

                @Override
                public void onSessionFinished(BlePeripheralDeviceConnection self, BlePeripheralDeviceConnection.Session session) {
                    updateDeviceValue(content, R.string.Word_Sensor_StateConnecting, -1, -1, -1);
                }
            }, new BleSpeedCadenceSensorCallback() {
                @Override
                public boolean onLoop(BleDeviceConnection self, BleGattController gatt) throws BluetoothException {
                    return false;
                }

                @Override
                protected void onUpdateBatteryLevel(int newLevel) {
                    updateDeviceValue(content, R.string.Word_Sensor_StateConnect, newLevel, -1, -1);
                }

                @Override
                protected void onUpdateCrankValue(RawSensorValue newValue, double crankRpm) {
                    int cadence = (int) Util.getDouble(getCrankRpm(), -1);
                    double speedKmh;
                    double wheelRpm = Util.getDouble(getWheelRpm(), -1);
                    if (wheelRpm < 0) {
                        speedKmh = -1;
                    } else {
                        speedKmh = calcSpeedKmPerHour(wheelRpm, WHEEL_LENGTH);
                    }

                    updateDeviceValue(content, R.string.Word_Sensor_StateConnect, Util.getInt(getBatteryLevel(), -1), cadence, (float) speedKmh);
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
    void updateDeviceValue(View content, @StringRes int statusText, int battery, int cadence, float speedKmh) {
        String BATTERY_TEXT = battery > 0 ? StringUtil.format("%d %%", battery) : getString(R.string.Word_Display_NoData);
        String CADENCE_TEXT = cadence > 0 ? StringUtil.format("%d rpm", cadence) : getString(R.string.Word_Display_NoData);
        String SPEED_TEXT = speedKmh > 0 ? StringUtil.format("%.1f km/h", speedKmh) : getString(R.string.Word_Display_NoData);
        UIHandler.postUIorRun(() -> {
            new AQuery(content)
                    .id(R.id.Item_Status).ifPresent(AppKeyValueView.class, view -> view.setValueText(getString(statusText)))
                    .id(R.id.Item_Updated).ifPresent(AppKeyValueView.class, view -> view.setValueText(TIME_FORMATTER.format(new Date())))
                    .id(R.id.Item_Battery).ifPresent(AppKeyValueView.class, view -> view.setValueText(BATTERY_TEXT))
                    .id(R.id.Item_Cadence).ifPresent(AppKeyValueView.class, view -> view.setValueText(CADENCE_TEXT))
                    .id(R.id.Item_Speed).ifPresent(AppKeyValueView.class, view -> view.setValueText(SPEED_TEXT));
        });
    }
}
