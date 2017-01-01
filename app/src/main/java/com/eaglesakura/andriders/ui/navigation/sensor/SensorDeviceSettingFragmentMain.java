package com.eaglesakura.andriders.ui.navigation.sensor;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.android.framework.ui.FragmentHolder;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;

/**
 * BLEデバイス等の設定を行う
 */
@FragmentLayout(R.layout.system_fragment_stack)
public class SensorDeviceSettingFragmentMain extends AppNavigationFragment {

    /**
     * センサー補助
     */
    FragmentHolder<SensorSupportSettingFragment> mSensorSupportSettingFragment =
            FragmentHolder.newInstance(this, SensorSupportSettingFragment.class, R.id.Content_List_Root).bind(mLifecycleDelegate);

    /**
     * BLE心拍センサー設定画面
     */
    FragmentHolder<BleHeartrateSettingFragment> mBleHeartrateScanner =
            FragmentHolder.newInstance(this, BleHeartrateSettingFragment.class, R.id.Content_List_Root).bind(mLifecycleDelegate);

    /**
     * BLEスピードセンサー設定画面
     */
    FragmentHolder<BleSpeedCadenceSettingFragment> mBleSpeedCadenceScanner =
            FragmentHolder.newInstance(this, BleSpeedCadenceSettingFragment.class, R.id.Content_List_Root).bind(mLifecycleDelegate);
}
