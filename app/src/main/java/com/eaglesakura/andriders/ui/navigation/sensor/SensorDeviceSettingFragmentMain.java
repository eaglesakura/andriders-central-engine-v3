package com.eaglesakura.andriders.ui.navigation.sensor;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.app.FragmentHolder;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;

/**
 * BLEデバイス等の設定を行う
 */
@FragmentLayout(R.layout.sensor_gadget)
public class SensorDeviceSettingFragmentMain extends AppNavigationFragment {

    /**
     * センサー補助
     */
    FragmentHolder<SensorSupportSettingFragment> mSensorSupportSettingFragment =
            FragmentHolder.newInstance(this, SensorSupportSettingFragment.class, R.id.Content_List_Root);

    /**
     * BLE心拍センサー設定画面
     */
    FragmentHolder<BleHeartrateSettingFragment> mBleHeartrateScanner =
            FragmentHolder.newInstance(this, BleHeartrateSettingFragment.class, R.id.Content_List_Root);

    /**
     * BLEスピードセンサー設定画面
     */
    FragmentHolder<BleSpeedCadenceSettingFragment> mBleSpeedCadenceScanner =
            FragmentHolder.newInstance(this, BleSpeedCadenceSettingFragment.class, R.id.Content_List_Root);

    @Override
    protected void onCreateLifecycle(FragmentLifecycle lifecycle) {
        super.onCreateLifecycle(lifecycle);
        mSensorSupportSettingFragment.bind(lifecycle);
        mBleHeartrateScanner.bind(lifecycle);
        mBleSpeedCadenceScanner.bind(lifecycle);
    }
}
