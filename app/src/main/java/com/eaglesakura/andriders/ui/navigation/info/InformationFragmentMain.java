package com.eaglesakura.andriders.ui.navigation.info;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.app.FragmentHolder;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;


/**
 * アプリ情報画面を管理する。
 * <p/>
 * * Central設定
 * <p/>
 * * ビルド情報
 */
@FragmentLayout(R.layout.system_fragment_stack)
public class InformationFragmentMain extends AppFragment {

    FragmentHolder<BuildInformationFragment> mBuildInformationFragment = FragmentHolder.newInstance(this, BuildInformationFragment.class, R.id.Content_List_Root);

    FragmentHolder<DeveloperInfoFragment> mDeveloperInfoFragment = FragmentHolder.newInstance(this, DeveloperInfoFragment.class, R.id.Content_List_Root);

    @Override
    protected void onCreateLifecycle(FragmentLifecycle lifecycle) {
        super.onCreateLifecycle(lifecycle);
        mBuildInformationFragment.bind(lifecycle);
        mDeveloperInfoFragment.bind(lifecycle);
    }
}
