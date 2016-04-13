package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseActivity;
import com.eaglesakura.andriders.ui.navigation.log.UserLogMain;
import com.eaglesakura.andriders.ui.navigation.menu.GoogleLoginCtrlFragment;
import com.eaglesakura.andriders.ui.navigation.menu.MenuController;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.BuildConfig;
import com.eaglesakura.android.framework.ui.BaseFragment;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.ContextUtil;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;

public class MainContentActivity extends AppBaseActivity {

    View progress;

    @Bind(R.id.Content_Drawer)
    DrawerLayout drawerLayout;

    MenuController menuController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // 初回は認証用Fragmentを入れる
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            {
                GoogleLoginCtrlFragment fragment = new GoogleLoginCtrlFragment();
                transaction.add(fragment, fragment.createSimpleTag());
            }
            transaction.commit();
        }

        menuController = new MenuController(this,
                findViewById(NavigationView.class, R.id.Content_Navigation_Root),
                drawerLayout
        );
        menuController.setCallback(mMenuCallback);
        menuController.initialize();

        progress = findViewById(R.id.Main_Progress);
//        userNotificationController = new UserNotificationController(this) {
//            @Override
//            protected void showProgressInterface(Object sender, String message) {
//                new AQuery(progress)
//                        .id(R.id.Main_Progress_Text).text(message)
//                        .id(R.id.Main_Progress).visible();
//            }
//
//            @Override
//            protected void updateProgressInterface(Object sender, String message) {
//            }
//
//            @Override
//            protected void dismissProgressInterface(Object sender) {
//                progress.setVisibility(View.INVISIBLE);
//            }
//        };

        if (!BuildConfig.DEBUG) {
            UIHandler.postDelayedUI(() -> {
                drawerLayout.openDrawer(GravityCompat.START);
            }, 500);
        }
    }

    @Override
    protected int getDefaultLayoutId() {
        return R.layout.activity_maincontent;
    }


    @Override
    protected BaseFragment newDefaultContentFragment() {
        return UserLogMain.newInstance(this);
//        return DisplaySettingFragmentMain.newInstance(this);
    }

    /**
     * 制御Fragmentを交換する。
     * 既にその画面が開かれている場合、何もしない。
     */
    void changeFragment(BaseNavigationFragment newFragment) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment oldFragment = getContentFragment();
        if (oldFragment != null && oldFragment.getClass().equals(newFragment.getClass())) {
            AppLog.system("Fragment not changed(%s)", newFragment.getClass());
            return;
        }

        FragmentTransaction transaction = manager.beginTransaction();
        {
            transaction.replace(R.id.Content_Holder_Root, newFragment, TAG_CONTENT_FRAGMENT_MAIN);
        }
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        menuController.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        menuController.onPause();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (ContextUtil.isBackKeyEvent(event)) {
            // progress中ならば作業を中断してはいけない
            if (progress.getVisibility() == View.VISIBLE) {
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    final MenuController.MenuCallback mMenuCallback = (fragment) -> {
        changeFragment(fragment);
    };
}
