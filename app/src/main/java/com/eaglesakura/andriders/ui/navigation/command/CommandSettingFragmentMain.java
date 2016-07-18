package com.eaglesakura.andriders.ui.navigation.command;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.NavigationBaseFragment;
import com.eaglesakura.andriders.ui.navigation.command.distance.DistanceCommandFragment;
import com.eaglesakura.andriders.ui.navigation.command.proximity.ProximityCommandFragment;
import com.eaglesakura.andriders.ui.navigation.command.speed.SpeedCommandFragment;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentPager;
import com.eaglesakura.android.margarine.Bind;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

/**
 * 各種コマンドを設定する
 */
public class CommandSettingFragmentMain extends NavigationBaseFragment {

    @Bind(R.id.Command_Main_PagerTab)
    TabLayout mPagerTab;

    @Bind(R.id.Command_Main_Pager)
    ViewPager mViewPager;

    SupportFragmentPager mFragmentPager = new SupportFragmentPager(R.id.Command_Main_Pager);

    public CommandSettingFragmentMain() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_command_main);
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        mFragmentPager.addFragment(SupportFragmentPager.newFragmentCreator(SpeedCommandFragment.class));
        mFragmentPager.addFragment(SupportFragmentPager.newFragmentCreator(ProximityCommandFragment.class));
        mFragmentPager.addFragment(SupportFragmentPager.newFragmentCreator(DistanceCommandFragment.class));

        // タブのセットアップ
        mViewPager.setAdapter(mFragmentPager.newAdapter(getChildFragmentManager()));
        mPagerTab.setupWithViewPager(mViewPager);
    }
}
