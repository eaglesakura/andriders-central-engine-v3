package com.eaglesakura.andriders.ui.navigation.profile;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.android.framework.ui.FragmentHolder;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;

/**
 * BLEデバイス等の設定を行う
 */
@FragmentLayout(R.layout.system_fragment_stack)
public class UserProfileSettingFragmentMain extends AppNavigationFragment {

    /**
     * ロードバイク基本設定
     */
    FragmentHolder<RoadbikeSettingFragment> mRoadbileSetting = FragmentHolder.newInstance(this, RoadbikeSettingFragment.class, R.id.Content_List_Root).bind(mLifecycleDelegate);
}
