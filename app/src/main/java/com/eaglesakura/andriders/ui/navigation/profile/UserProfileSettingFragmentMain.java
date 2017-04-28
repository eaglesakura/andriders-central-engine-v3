package com.eaglesakura.andriders.ui.navigation.profile;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.app.FragmentHolder;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;

/**
 * BLEデバイス等の設定を行う
 */
@FragmentLayout(R.layout.system_fragment_stack)
public class UserProfileSettingFragmentMain extends AppNavigationFragment {

    /**
     * ロードバイク基本設定
     */
    FragmentHolder<RoadbikeSettingFragment> mRoadbileSettingFragment = FragmentHolder.newInstance(this, RoadbikeSettingFragment.class, R.id.Content_List_Root);

    /**
     * ゾーン設定
     */
    FragmentHolder<ZoneSettingFragment> mZoneSettingFragment = FragmentHolder.newInstance(this, ZoneSettingFragment.class, R.id.Content_List_Root);

    /**
     * 身体能力設定
     */
    FragmentHolder<FitnessSettingFragment> mFitnessSettingFragment = FragmentHolder.newInstance(this, FitnessSettingFragment.class, R.id.Content_List_Root);

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    @Override
    protected void onCreateLifecycle(FragmentLifecycle lifecycle) {
        super.onCreateLifecycle(lifecycle);
        mRoadbileSettingFragment.bind(lifecycle);
        mZoneSettingFragment.bind(lifecycle);
        mFitnessSettingFragment.bind(lifecycle);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAppSettings.commit();
    }
}
