package com.eaglesakura.andriders.ui.navigation.command;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.navigation.command.distance.DistanceCommandFragment;
import com.eaglesakura.andriders.ui.navigation.command.proximity.ProximityCommandFragment;
import com.eaglesakura.andriders.ui.navigation.command.speed.SpeedCommandFragment;
import com.eaglesakura.andriders.ui.navigation.command.timer.TimerCommandFragment;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.ui.pager.SupportFragmentPager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 各種コマンドを設定する
 */
@FragmentLayout(R.layout.command_setup)
public class CommandSettingFragmentMain extends AppFragment {
    @Bind(R.id.Content_PagerTab)
    TabLayout mPagerTab;

    @Bind(R.id.Content_Pager)
    ViewPager mViewPager;

    SupportFragmentPager mFragmentPager = new SupportFragmentPager(R.id.Content_Pager);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mFragmentPager.addFragment(SupportFragmentPager.newFragmentCreator(ProximityCommandFragment.class));
        mFragmentPager.addFragment(SupportFragmentPager.newFragmentCreator(SpeedCommandFragment.class));
        mFragmentPager.addFragment(SupportFragmentPager.newFragmentCreator(DistanceCommandFragment.class));
        mFragmentPager.addFragment(SupportFragmentPager.newFragmentCreator(TimerCommandFragment.class));

        // タブのセットアップ
        mViewPager.setAdapter(mFragmentPager.newAdapter(getChildFragmentManager()));
        mPagerTab.setupWithViewPager(mViewPager);
        return view;
    }

}
