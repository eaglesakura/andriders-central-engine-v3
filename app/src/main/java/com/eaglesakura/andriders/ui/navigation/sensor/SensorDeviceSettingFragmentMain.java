package com.eaglesakura.andriders.ui.navigation.sensor;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.model.ble.BleDeviceType;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.android.framework.ui.FragmentHolder;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * BLEデバイス等の設定を行う
 */
@FragmentLayout(R.layout.system_fragment_stack)
public class SensorDeviceSettingFragmentMain extends AppNavigationFragment {

    FragmentHolder<BleFitnessSensorSettingFragment> mBleHeartrateScanner = new FragmentHolder<BleFitnessSensorSettingFragment>(this, R.id.Content_List_Root, "BLE.HR") {
        @NonNull
        @Override
        protected BleFitnessSensorSettingFragment newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception {
            BleFitnessSensorSettingFragment fragment = new BleFitnessSensorSettingFragment();
            fragment.setDeviceType(BleDeviceType.HEARTRATE_MONITOR);
            return fragment;
        }
    }.bind(mLifecycleDelegate);
}
