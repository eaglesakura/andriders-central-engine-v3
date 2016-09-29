package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseActivity;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;
import com.eaglesakura.android.framework.ui.FragmentTransactionBuilder;
import com.eaglesakura.android.util.ContextUtil;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

/**
 * Created by eaglesakura on 2016/07/09.
 */
public abstract class NavigationBaseActivity extends AppBaseActivity {
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (ContextUtil.isBackKeyEvent(event)) {
        }

        return super.dispatchKeyEvent(event);
    }
}
