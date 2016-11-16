package com.eaglesakura.andriders.ui.navigation.sensor;

import com.google.android.gms.fitness.data.BleDevice;
import com.google.android.gms.fitness.request.BleScanCallback;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.model.ble.BleDeviceScanner;
import com.eaglesakura.andriders.model.ble.BleDeviceType;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.ResourceUtil;
import com.eaglesakura.material.widget.SnackbarBuilder;
import com.eaglesakura.material.widget.SpinnerAdapterBuilder;
import com.eaglesakura.util.StringUtil;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Google Fitによるスキャン可能なデバイスをスキャン・選択するUI
 */
@FragmentLayout(R.layout.sensor_gadgets_ble_fitness)
public class BleFitnessSensorSettingFragment extends AppFragment {

    @BundleState
    int mDeviceTypeId;

    @NonNull
    BleDeviceType mDeviceType;

    @NonNull
    BleDeviceScanner mScanner;

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    ArrayAdapter<BleDevice> mSpinnerAdapter;

    public void setDeviceType(@NonNull BleDeviceType deviceType) {
        mDeviceType = deviceType;
        mDeviceTypeId = mDeviceType.getDeviceTypeId();
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
        AQuery q = new AQuery(self.getView());

        List<BleDevice> devices = new ArrayList<>();
        devices.add(null);
        SpinnerAdapterBuilder.from(q.id(R.id.Selector_Device).getSpinner(), BleDevice.class)
                .items(devices, it -> {
                    if (it == null) {
                        return getString(R.string.Setting_Gadgets_BleDevice_NotConnected);
                    } else {
                        String address = StringUtil.replaceAllSimple(it.getAddress(), ":", "").substring(0, 6).toUpperCase();
                        return it.getName() + " [" + address + "]";
                    }
                })
                .build();
        mSpinnerAdapter = (ArrayAdapter<BleDevice>) q.getSpinner().getAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDeviceType == null) {
            mDeviceType = BleDeviceType.fromId(mDeviceTypeId);
        }

        if (mDeviceType == BleDeviceType.HEARTRATE_MONITOR) {
            new AQuery(getView())
                    .id(R.id.App_HeaderView_Icon).image(ResourceUtil.vectorDrawable(getContext(), R.drawable.ic_heart_beats, R.color.App_Icon_Grey))
                    .id(R.id.App_HeaderView_Title).text(R.string.Setting_Gadgets_Heartrate);
        } else {
            new AQuery(getView())
                    .id(R.id.App_HeaderView_Icon).image(ResourceUtil.vectorDrawable(getContext(), R.drawable.ic_speed, R.color.App_Icon_Grey))
                    .id(R.id.App_HeaderView_Title).text(R.string.Setting_Gadgets_SpeedAndCadence);
        }

        mScanner = new BleDeviceScanner(getContext(), mDeviceType);
        mScanner.setBleScanCallback(mBleScanCallback);
        mScanner.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScanner.disconnect();
        mScanner = null;
    }

    /**
     * 発見したデバイスをSpinnerに登録する
     */
    @UiThread
    void onDeviceFound(BleDevice device) {
        int position = mSpinnerAdapter.getPosition(device);
        if (position >= 0) {
            // すでに追加済み
            return;
        }

        mSpinnerAdapter.add(device);
        mSpinnerAdapter.notifyDataSetChanged();

        SnackbarBuilder.from(this)
                .message(getString(R.string.Message_Sensor_Found, device.getName()))
                .show();
    }

    final BleScanCallback mBleScanCallback = new BleScanCallback() {
        @Override
        public void onDeviceFound(BleDevice bleDevice) {
            UIHandler.postUI(() -> BleFitnessSensorSettingFragment.this.onDeviceFound(bleDevice));
        }

        @Override
        public void onScanStopped() {

        }
    };
}
