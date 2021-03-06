package com.eaglesakura.andriders.ui.navigation.command;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.navigation.command.distance.DistanceCommandFragment;
import com.eaglesakura.andriders.ui.navigation.command.proximity.ProximityCommandFragment;
import com.eaglesakura.andriders.ui.navigation.command.speed.SpeedCommandFragment;
import com.eaglesakura.andriders.ui.navigation.command.timer.TimerCommandFragment;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentPager;
import com.eaglesakura.android.margarine.Bind;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

/**
 * 各種コマンドを設定する
 */
public class CommandSettingFragmentMain extends AppFragment {
    @Bind(R.id.Content_PagerTab)
    TabLayout mPagerTab;

    @Bind(R.id.Content_Pager)
    ViewPager mViewPager;

    SupportFragmentPager mFragmentPager = new SupportFragmentPager(R.id.Content_Pager);

    public CommandSettingFragmentMain() {
        mFragmentDelegate.setLayoutId(R.layout.command_setup);
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        mFragmentPager.addFragment(SupportFragmentPager.newFragmentCreator(ProximityCommandFragment.class));
        mFragmentPager.addFragment(SupportFragmentPager.newFragmentCreator(SpeedCommandFragment.class));
        mFragmentPager.addFragment(SupportFragmentPager.newFragmentCreator(DistanceCommandFragment.class));
        mFragmentPager.addFragment(SupportFragmentPager.newFragmentCreator(TimerCommandFragment.class));

        // タブのセットアップ
        mViewPager.setAdapter(mFragmentPager.newAdapter(getChildFragmentManager()));
        mPagerTab.setupWithViewPager(mViewPager);
    }


}
