package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.framework.ui.BaseActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import butterknife.Bind;

/**
 * 各画面を統括するFragmentのsuper class
 */
public class BaseNavigationFragment extends AppBaseFragment {
    /**
     * Toolbarが存在するなら取得する
     */
    @Bind(R.id.EsMaterial_Toolbar)
    protected Toolbar toolbar;

    private ActionBarDrawerToggle drawerToggle;

    private DrawerLayout drawerLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onAfterViews() {
        super.onAfterViews();
        ((BaseActivity) getActivity()).setSupportActionBar(toolbar);
        drawerLayout = findViewByIdFromActivity(DrawerLayout.class, R.id.Content_Drawer);
        initializeDrawerToggle();
    }

    private void initializeDrawerToggle() {
        if (toolbar == null || drawerToggle != null) {
            return;
        }

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        toolbar = findViewByIdFromActivity(Toolbar.class, R.id.EsMaterial_Toolbar);

        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, 0, 0) {
            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public void closeNavigationDrawer() {
        drawerLayout.closeDrawers();
    }
}
