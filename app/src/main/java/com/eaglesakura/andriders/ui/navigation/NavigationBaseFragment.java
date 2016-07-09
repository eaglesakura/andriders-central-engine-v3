package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.progress.ProgressStackManager;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.material.widget.MaterialProgressView;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

/**
 * 各画面を統括するFragmentのsuper class
 */
public class NavigationBaseFragment extends AppBaseFragment {
    /**
     * Toolbarが存在するなら取得する
     */
    @Bind(R.id.EsMaterial_Toolbar)
    protected Toolbar mToolbar;

    @Nullable
    private ActionBarDrawerToggle mDrawerToggle;

    @Nullable
    DrawerLayout mDrawerLayout;

    @NonNull
    private final ProgressStackManager mProgressStackManager = new ProgressStackManager(mLifecycleDelegate.getSubscription());

    public NavigationBaseFragment() {
        mProgressStackManager.setListener(mStackManagerListener);
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        mDrawerLayout = findViewByIdFromActivity(DrawerLayout.class, R.id.Content_Drawer);
        if (mToolbar != null) {
            // ActionBarと関連付ける
            getActivity(AppCompatActivity.class).setSupportActionBar(mToolbar);
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);

            if (mDrawerLayout != null) {
                initializeDrawerToggle();
            }
        }
    }

    private void initializeDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, mToolbar, 0, 0) {
            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    /**
     * 次の画面遷移を行う
     */
    public void nextNavigation(NavigationBaseFragment fragment, int flags) {
        getActivity(NavigationBaseActivity.class).nextNavigation(fragment, flags);
    }


    public ProgressStackManager getProgressStackManager() {
        return mProgressStackManager;
    }

    private ProgressStackManager.Listener mStackManagerListener = new ProgressStackManager.Listener() {
        @Override
        public void onProgressStarted(@NonNull ProgressStackManager self, @NonNull ProgressToken token) {
            AQuery q = new AQuery(getView());
            q.id(R.id.App_Progress)
                    .ifPresent(MaterialProgressView.class, it -> {
                        it.setText(token.getMessage());
                        it.setVisibility(View.VISIBLE);
                    });
        }

        @Override
        public void onProgressTopChanged(@NonNull ProgressStackManager self, @NonNull ProgressToken topToken) {
            AQuery q = new AQuery(getView());
            q.id(R.id.App_Progress)
                    .ifPresent(MaterialProgressView.class, it -> {
                        it.setText(topToken.getMessage());
                        it.setVisibility(View.VISIBLE);
                    });
        }

        @Override
        public void onProgressFinished(@NonNull ProgressStackManager self) {
            AQuery q = new AQuery(getView());
            q.id(R.id.App_Progress)
                    .ifPresent(MaterialProgressView.class, it -> {
                        it.setVisibility(View.INVISIBLE);
                    });
        }
    };

    public void closeNavigationDrawer() {
        if (mDrawerToggle != null) {
            mDrawerLayout.closeDrawers();
        }
    }
}
