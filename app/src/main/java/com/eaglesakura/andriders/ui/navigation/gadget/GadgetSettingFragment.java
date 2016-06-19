package com.eaglesakura.andriders.ui.navigation.gadget;

import com.google.android.gms.fitness.data.BleDevice;
import com.google.android.gms.fitness.request.BleScanCallback;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.dao.bledevice.DbBleFitnessDevice;
import com.eaglesakura.andriders.db.fit.FitnessDeviceCacheDatabase;
import com.eaglesakura.andriders.google.FitnessDeviceController;
import com.eaglesakura.andriders.google.FitnessDeviceType;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.v2.db.UserProfiles;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.ui.spinner.BasicSpinnerAdapter;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.bundle.BundleState;
import com.eaglesakura.util.LogUtil;

import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

public class GadgetSettingFragment extends AppBaseFragment {

    @BundleState
    boolean mScanBleDevice = false;

    final List<DbBleFitnessDevice> mHeartrateDevices = new ArrayList<>();

    FitnessDeviceController mHeartrateController;

    BasicSpinnerAdapter mHeartrateAdapter;

    final List<DbBleFitnessDevice> mCadenceDevices = new ArrayList<>();

    FitnessDeviceController mCadenceSpeedController;

    BasicSpinnerAdapter mCadenceAdapter;

    UserProfiles mPersonalDataSettings;

    public GadgetSettingFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_setting_gadgets);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPersonalDataSettings = getSettings().getUserProfiles();
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
        AQuery q = new AQuery(getView());

        // ハートレートモニター設定
        {
            FitnessDeviceCallbackImpl callback = new FitnessDeviceCallbackImpl(FitnessDeviceType.HEARTRATE_MONITOR, mHeartrateDevices);

            mHeartrateController = FitnessDeviceType.HEARTRATE_MONITOR.createController(getActivity());
            mHeartrateController.setBleScanCallback(callback);
            mHeartrateAdapter = new BasicSpinnerAdapter(getActivity());
            q.id(R.id.Setting_RoadBikeProfile_HeartrateMonitor_Selector)
                    .adapter(mHeartrateAdapter)
                    .itemSelected(callback);
        }

        // ケイデンスセンサー設定
        {
            FitnessDeviceCallbackImpl callback = new FitnessDeviceCallbackImpl(FitnessDeviceType.SPEED_CADENCE_SENSOR, mCadenceDevices);

            mCadenceSpeedController = FitnessDeviceType.SPEED_CADENCE_SENSOR.createController(getActivity());
            mCadenceSpeedController.setBleScanCallback(callback);
            mCadenceAdapter = new BasicSpinnerAdapter(getActivity());
            q.id(R.id.Setting_RoadBikeProfile_SpeedAndCadence_Selector)
                    .adapter(mCadenceAdapter)
                    .itemSelected(callback);
        }
    }

    @OnClick(R.id.Setting_RoadBikeProfile_BleDevice_ScanRequest)
    void clickScanRequest() {
        if (requestRuntimePermission(PermissionUtil.PermissionType.BluetoothLE)) {
            return;
        }

        mScanBleDevice = true;
        mHeartrateController.connect();
        mCadenceSpeedController.connect();

        AQuery q = new AQuery(getView());
        q.id(R.id.Setting_RoadBikeProfile_BleDevice_ScanRequest).gone();
        q.id(R.id.Setting_RoadBikeProfile_BleDevice_ScanNowRoot).visible();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResume() {
        super.onResume();

        // 必要であればスキャンを復旧させる
        if (mScanBleDevice) {
            mHeartrateController.connect();
            mCadenceSpeedController.connect();
        }

        // デバイスを読み込む
        loadDeviceCache(FitnessDeviceType.HEARTRATE_MONITOR);
        loadDeviceCache(FitnessDeviceType.SPEED_CADENCE_SENSOR);
    }

    @Override
    public void onPause() {
        super.onPause();

        // スキャンを終了させる
        mHeartrateController.disconnect();
        mCadenceSpeedController.disconnect();
    }

    /**
     * デバイス情報を読み込む
     */
    @UiThread
    void loadDeviceCache(final FitnessDeviceType type) {
        asyncUI(it -> {
            FitnessDeviceCacheDatabase db = new FitnessDeviceCacheDatabase(getActivity());
            try {
                pushProgress(R.string.Common_File_Load);
                db.openReadOnly();

                List<DbBleFitnessDevice> devices = db.listScanDevices(type);

                String address;
                if (type == FitnessDeviceType.HEARTRATE_MONITOR) {
                    address = mPersonalDataSettings.getBleHeartrateMonitorAddress();
                } else if (type == FitnessDeviceType.SPEED_CADENCE_SENSOR) {
                    address = mPersonalDataSettings.getBleSpeedCadenceSensorAddress();
                } else {
                    throw new IllegalStateException();
                }
                DbBleFitnessDevice selected = db.load(address);
                updateBleSelectorUI(type, devices, selected != null ? selected.getAddress() : null);
            } finally {
                db.close();
                popProgress();
            }
            return null;
        }).start();
    }

    /**
     * UIを更新する
     */
    @UiThread
    void updateBleSelectorUI(final FitnessDeviceType fitnessDevice, final List<DbBleFitnessDevice> devices, final String selectedDeviceAddress) {
        getSubscription().run(ObserveTarget.Foreground, () -> {
            BasicSpinnerAdapter adapter;
            int selectorLayoutId;
            List<DbBleFitnessDevice> devicesList;
            if (fitnessDevice == FitnessDeviceType.HEARTRATE_MONITOR) {
                adapter = mHeartrateAdapter;
                selectorLayoutId = R.id.Setting_RoadBikeProfile_HeartrateMonitor_Selector;
                devicesList = mHeartrateDevices;
            } else if (fitnessDevice == FitnessDeviceType.SPEED_CADENCE_SENSOR) {
                adapter = mCadenceAdapter;
                selectorLayoutId = R.id.Setting_RoadBikeProfile_SpeedAndCadence_Selector;
                devicesList = mCadenceDevices;
            } else {
                throw new IllegalArgumentException();
            }

            AQuery q = new AQuery(getView()).id(selectorLayoutId);
            // デバイスリストに登録する
            {
                // 先頭にnullを突っ込み、非選択版として扱う
                devices.add(null);
                devicesList.clear();
                devicesList.addAll(devices);
            }

            // 選択用のリストを生成する
            adapter.clear();
            int selected = -1;
            int count = 0;
            for (DbBleFitnessDevice device : devices) {
                if (device != null) {
//                        adapter.add(getString(R.string.Setting_Gadgets_Sensors_Selector, device.getName(), device.getSelectedCount()));
                    adapter.add(getString(R.string.Setting_Gadgets_Sensors_Selector, device.getName(), toDeviceId(device.getAddress())));
                    if (device.getAddress().equals(selectedDeviceAddress)) {
                        selected = count;
                    }
                    ++count;
                } else {
                    adapter.add(getString(R.string.Setting_Gadgets_Sensors_Selector_None));
                }
            }

            if (selected < 0) {
                // 明示的な選択が無ければ最後を選択する
                selected = devices.size() - 1;
            }

            adapter.notifyDataSetChanged();
            // 既にされていれば、カーソルを選択する
            if (selected >= 0) {
                q.setSelection(selected);
            }
        });
    }

    private String toDeviceId(String addr) {
        addr = addr.replaceAll(":", "");
        return addr.substring(0, 6);
    }

    class FitnessDeviceCallbackImpl extends BleScanCallback implements AdapterView.OnItemSelectedListener {
        final FitnessDeviceType type;
        final List<DbBleFitnessDevice> devices;

        FitnessDeviceCallbackImpl(FitnessDeviceType device, List<DbBleFitnessDevice> devices) {
            this.type = device;
            this.devices = devices;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            DbBleFitnessDevice device = devices.get(position);
            String address = (device == null ? "" : device.getAddress());
            if (type == FitnessDeviceType.HEARTRATE_MONITOR) {
                mPersonalDataSettings.setBleHeartrateMonitorAddress(address);
            } else if (type == FitnessDeviceType.SPEED_CADENCE_SENSOR) {
                mPersonalDataSettings.setBleSpeedCadenceSensorAddress(address);
            } else {
                throw new IllegalStateException();
            }
            asyncCommitSettings();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

        @Override
        public void onDeviceFound(final BleDevice bleDevice) {
            toast(getString(R.string.Setting_Gadgets_Sensors_Found, bleDevice.getName()));
            asyncUI(it -> {
                FitnessDeviceCacheDatabase db = new FitnessDeviceCacheDatabase(getActivity());
                try {
                    db.openWritable();
                    db.foundDevice(type, bleDevice);
                } catch (Exception e) {
                    LogUtil.log(e);
                } finally {
                    db.close();
                }

                // リロードを行う
                loadDeviceCache(type);
                return null;
            }).start();
        }

        @Override
        public void onScanStopped() {
        }
    }
}
