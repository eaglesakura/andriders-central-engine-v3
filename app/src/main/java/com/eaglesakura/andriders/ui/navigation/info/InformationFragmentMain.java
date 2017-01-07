package com.eaglesakura.andriders.ui.navigation.info;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.android.framework.ui.FragmentHolder;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;


/**
 * アプリ情報画面を管理する。
 * <p/>
 * * Central設定
 * <p/>
 * * ビルド情報
 */
@FragmentLayout(R.layout.system_fragment_stack)
public class InformationFragmentMain extends AppFragment {

    FragmentHolder<BuildInformationFragment> mBuildInformationFragment = FragmentHolder.newInstance(this, BuildInformationFragment.class, R.id.Content_List_Root).bind(mLifecycleDelegate);

    FragmentHolder<DeveloperInfoFragment> mDeveloperInfoFragment = FragmentHolder.newInstance(this, DeveloperInfoFragment.class, R.id.Content_List_Root).bind(mLifecycleDelegate);
}
