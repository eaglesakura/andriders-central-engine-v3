package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;
import com.eaglesakura.andriders.ui.navigation.MainContentActivity;
import com.eaglesakura.andriders.ui.navigation.log.gpx.GpxTourFragmentMain;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentPager;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.OnMenuClick;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import icepick.State;

/**
 * ログ表示画面のメインFragment
 */
public class UserLogFragmentMain extends BaseNavigationFragment {

    @NonNull
    final SupportFragmentPager mPager = new SupportFragmentPager(R.id.UserActivity_Main_Pager);

    @Bind(R.id.UserActivity_Main_Pager)
    ViewPager mViewPager;

    @State
    int mCurrentPage;

    public UserLogFragmentMain() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_user_log_main);
        mFragmentDelegate.setOptionMenuId(R.menu.fragment_userlog_import);
        mPager.addFragment(SupportFragmentPager.newFragmentCreator(UserLogSynthesisFragment.class));
        mPager.addFragment(SupportFragmentPager.newFragmentCreator(UserLogDailyFragment.class));
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
        mViewPager.setAdapter(mPager.newAdapter(getChildFragmentManager()));
        mViewPager.setCurrentItem(mCurrentPage);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mCurrentPage = mViewPager.getCurrentItem();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
        }
    }

    @OnMenuClick(R.id.UserLog_Import_GPX)
    public void clickImportGpx() {
        nextNavigation(new GpxTourFragmentMain(), MainContentActivity.NAVIGATION_FLAG_BACKSTACK);
    }

    public static UserLogFragmentMain newInstance(Context context) {
        return new UserLogFragmentMain();
    }
}
