package com.eaglesakura.andriders.ui.navigation.profile;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.android.framework.ui.FragmentHolder;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.garnet.Inject;

/**
 * BLEデバイス等の設定を行う
 */
@FragmentLayout(R.layout.system_fragment_stack)
public class UserProfileSettingFragmentMain extends AppNavigationFragment {

    /**
     * ロードバイク基本設定
     */
    FragmentHolder<RoadbikeSettingFragment> mRoadbileSettingFragment = FragmentHolder.newInstance(this, RoadbikeSettingFragment.class, R.id.Content_List_Root).bind(mLifecycleDelegate);

    /**
     * ゾーン設定
     */
    FragmentHolder<ZoneSettingFragment> mZoneSettingFragment = FragmentHolder.newInstance(this, ZoneSettingFragment.class, R.id.Content_List_Root).bind(mLifecycleDelegate);

    /**
     * 身体能力設定
     */
    FragmentHolder<FitnessSettingFragment> mFitnessSettingFragment = FragmentHolder.newInstance(this, FitnessSettingFragment.class, R.id.Content_List_Root).bind(mLifecycleDelegate);

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;


    @Override
    public void onPause() {
        super.onPause();
        mAppSettings.commit();
    }
}
