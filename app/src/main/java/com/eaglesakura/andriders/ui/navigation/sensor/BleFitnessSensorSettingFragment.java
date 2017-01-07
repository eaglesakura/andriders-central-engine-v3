package com.eaglesakura.andriders.ui.navigation.sensor;

import com.google.android.gms.fitness.data.BleDevice;
import com.google.android.gms.fitness.request.BleScanCallback;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.data.sensor.SensorDataManager;
import com.eaglesakura.andriders.model.ble.BleDeviceCache;
import com.eaglesakura.andriders.model.ble.BleDeviceScanner;
import com.eaglesakura.andriders.model.ble.BleDeviceType;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.provider.AppManagerProvider;
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
import com.eaglesakura.material.widget.SupportArrayAdapter;
import com.eaglesakura.util.StringUtil;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Google Fitによるスキャン可能なデバイスをスキャン・選択するUI
 */
@FragmentLayout(R.layout.sensor_gadgets_ble_fitness)
public abstract class BleFitnessSensorSettingFragment extends AppFragment {

    static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");

    @BundleState
    int mDeviceTypeId;

    @NonNull
    BleDeviceType mDeviceType;

    @NonNull
    BleDeviceScanner mScanner;

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    @NonNull
    SupportArrayAdapter<BleDeviceCache> mSpinnerAdapter;

    @Inject(AppManagerProvider.class)
    SensorDataManager mSensorDataManager;

    @BundleState
    @DrawableRes
    int mHeaderIconRes;

    @BundleState
    @StringRes
    int mHeaderTextRes;

    /**
     * Propに保存するときのKey指定
     */
    @BundleState
    String mDeviceAddressPropertyKey;

    /**
     * 初期化を行う
     *
     * @param deviceType               スキャン対象デバイス
     * @param headerIconRes            ヘッダアイコン
     * @param headerTextRes            ヘッダテキスト
     * @param deviceAddressPropertyKey 保存時のKey
     */
    public void initialize(@NonNull BleDeviceType deviceType, @DrawableRes int headerIconRes, @StringRes int headerTextRes, @NonNull String deviceAddressPropertyKey) {
        mDeviceType = deviceType;
        mHeaderIconRes = headerIconRes;
        mHeaderTextRes = headerTextRes;
        mDeviceAddressPropertyKey = deviceAddressPropertyKey;
        mDeviceTypeId = mDeviceType.getDeviceTypeId();
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
        AQuery q = new AQuery(self.getView());

        List<BleDeviceCache> devices = new ArrayList<>();
        devices.add(null);
        devices.addAll(mSensorDataManager.load(mDeviceType).list());
        SpinnerAdapterBuilder.from(q.id(R.id.Selector_Device).getSpinner(), BleDeviceCache.class)
                .items(devices)
                .title((index, it) -> {
                    if (it == null) {
                        return getString(R.string.Word_Gadget_NoDevice);
                    } else {
                        return it.getDisplayName(getContext());
                    }
                })
                .selection(it -> {
                    if (it == null) {
                        return false;
                    }

                    Set<String> selected = new HashSet<>();
                    selected.add(mAppSettings.getUserProfiles().getBleHeartrateMonitorAddress());
                    selected.add(mAppSettings.getUserProfiles().getBleSpeedCadenceSensorAddress());

                    for (String address : selected) {
                        if (!StringUtil.isEmpty(address) && address.equals(it.getAddress())) {
                            return true;
                        }
                    }
                    return false;
                })
                .selected(it -> onDeviceSelected(it))
                .build();
        mSpinnerAdapter = (SupportArrayAdapter<BleDeviceCache>) q.getSpinner().getAdapter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mDeviceType == null) {
            mDeviceType = BleDeviceType.fromId(mDeviceTypeId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // ヘッダ表記を変更する
        new AQuery(getView())
                .id(R.id.App_HeaderView_Icon).image(ResourceUtil.vectorDrawable(getContext(), mHeaderIconRes, R.color.App_Icon_Grey))
                .id(R.id.App_HeaderView_Title).text(mHeaderTextRes);

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

    @UiThread
    void onDeviceSelected(BleDeviceCache device) {
        String address = (device != null ? device.getAddress() : "");

        mAppSettings.getUserProfiles().setProperty(mDeviceAddressPropertyKey, address);
        mAppSettings.commit();
    }

    /**
     * 発見したデバイスをSpinnerに登録する
     */
    @UiThread
    void onDeviceFound(BleDeviceCache device) {
        // 最新のキャッシュに交換する
        mSensorDataManager.save(device);

        // 検出通知は行う
        SnackbarBuilder.from(this)
                .message(R.string.Message_Gadget_Found, device.getName())
                .show();

        for (int i = 0; i < mSpinnerAdapter.getCount(); ++i) {
            if (device.equals(mSpinnerAdapter.getItem(i))) {
                // すでに登録されていた
                return;
            }
        }

        mSpinnerAdapter.add(device);
        mSpinnerAdapter.notifyDataSetChanged();
    }

    final BleScanCallback mBleScanCallback = new BleScanCallback() {
        @Override
        public void onDeviceFound(BleDevice bleDevice) {
            UIHandler.postUI(() -> BleFitnessSensorSettingFragment.this.onDeviceFound(new BleDeviceCache(bleDevice)));
        }

        @Override
        public void onScanStopped() {

        }
    };
}
