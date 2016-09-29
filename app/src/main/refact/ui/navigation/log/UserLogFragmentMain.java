package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.db.session.SessionTotalCollection;
import com.eaglesakura.andriders.ui.navigation.NavigationBaseActivity;
import com.eaglesakura.andriders.ui.navigation.NavigationBaseFragment;
import com.eaglesakura.andriders.ui.navigation.log.gpx.GpxTourFragmentMain;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentPager;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.OnMenuClick;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.saver.BundleState;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.view.ViewPager;

/**
 * ログ表示画面のメインFragment
 */
public class UserLogFragmentMain extends NavigationBaseFragment implements UserLogFragmentParent {

    @NonNull
    final SupportFragmentPager mPager = new SupportFragmentPager(R.id.UserActivity_Main_Pager);

    SessionTotalCollection mSessionTotalCollection;

    @Bind(R.id.UserActivity_Main_Pager)
    ViewPager mViewPager;

    @BundleState
    int mCurrentPage;

    public UserLogFragmentMain() {
        mFragmentDelegate.setLayoutId(R.layout.user_log);
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
    public void onResume() {
        super.onResume();
        loadSessionTotal();
    }

    @UiThread
    void loadSessionTotal() {
        mSessionTotalCollection = null;
        asyncUI((BackgroundTask<SessionTotalCollection> task) -> {
            SessionLogDatabase db = new SessionLogDatabase(getActivity());
            try {
                db.openReadOnly();
                return db.loadTotal(SessionTotalCollection.Order.Desc);
            } finally {
                db.close();
            }
        }).completed((result, task) -> {
            mSessionTotalCollection = result;
        }).start();
    }

    @Nullable
    @Override
    public SessionTotalCollection getUserLogCollection() {
        return mSessionTotalCollection;
    }

    @OnMenuClick(R.id.UserLog_Import_GPX)
    public void clickImportGpx() {
        nextNavigation(new GpxTourFragmentMain(), NavigationBaseActivity.NAVIGATION_FLAG_BACKSTACK);
    }

    public static UserLogFragmentMain newInstance(Context context) {
        return new UserLogFragmentMain();
    }
}
