package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseActivity;
import com.eaglesakura.andriders.ui.navigation.display.DisplaySettingFragmentMain;
import com.eaglesakura.andriders.ui.navigation.menu.GoogleLoginCtrlFragment;
import com.eaglesakura.andriders.ui.navigation.menu.MenuController;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.BuildConfig;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;
import com.eaglesakura.android.framework.ui.FragmentTransactionBuilder;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.ContextUtil;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;

public class NavigationActivity extends AppBaseActivity {

    View mProgress;

    @Bind(R.id.Content_Drawer)
    DrawerLayout mDrawerLayout;

    MenuController mMenuController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // 初回は認証用Fragmentを入れる
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            {
                GoogleLoginCtrlFragment fragment = new GoogleLoginCtrlFragment();
                transaction.add(fragment, fragment.getClass().getName());
            }
            transaction.commit();
        }

        mMenuController = new MenuController(this,
                findViewById(NavigationView.class, R.id.Content_Navigation_Root),
                mDrawerLayout
        );
        mMenuController.setCallback(mMenuCallback);
        mProgress = findViewById(R.id.Main_Progress);
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
                mDrawerLayout.openDrawer(GravityCompat.START);
            }, 500);
        }
    }

    @Override
    public int getDefaultLayoutId(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.activity_maincontent;
    }

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
//        return UserLogMain.newInstance(this);
        return DisplaySettingFragmentMain.newInstance(this);
//        return GpxImportTourFragmentMain.newInstance(this);
    }

    /**
     * BackStackに追加する
     */
    public static final int NAVIGATION_FLAG_BACKSTACK = 0x1 << 0;

    /**
     * 制御Fragmentを交換する。
     * 既にその画面が開かれている場合、何もしない。
     */
    public void nextNavigation(Fragment newFragment, int flags) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment oldFragment = getContentFragment();
        if (oldFragment != null && oldFragment.getClass().equals(newFragment.getClass())) {
            AppLog.system("Fragment not changed(%s)", newFragment.getClass());
            return;
        }

        FragmentTransactionBuilder builder = new FragmentTransactionBuilder(this, manager);
        builder.animation(FragmentTransactionBuilder.AnimationType.Fade)
                .replace(R.id.Content_Holder_Root, newFragment, ContentHolderActivityDelegate.TAG_CONTENT_FRAGMENT_MAIN);
        if ((flags & NAVIGATION_FLAG_BACKSTACK) != 0) {
            builder.addToBackStack();
        }
        builder.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMenuController.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMenuController.onPause();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (ContextUtil.isBackKeyEvent(event)) {
            // progress中ならば作業を中断してはいけない
            if (mProgress.getVisibility() == View.VISIBLE) {
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    final MenuController.MenuCallback mMenuCallback = (fragment) -> {
        nextNavigation(fragment, 0x00);
    };
}
