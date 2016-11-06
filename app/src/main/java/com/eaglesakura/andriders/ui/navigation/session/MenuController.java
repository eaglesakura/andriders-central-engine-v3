package com.eaglesakura.andriders.ui.navigation.session;

import com.eaglesakura.andriders.R;
import com.eaglesakura.android.framework.delegate.lifecycle.LifecycleDelegate;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.material.widget.ToolbarBuilder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

/**
 * メニュー内容を動的に制御するクラス。
 * <p/>
 * 将来的に、拡張モジュールごとにメニューを割り当てることが考えられる。
 * そのため、menuリソースを使わず、プログラム側でコントロールを行う。
 */
public class MenuController {
    @NonNull
    Context mContext;

    @NonNull
    NavigationView mNavigationView;

    @NonNull
    DrawerLayout mDrawerLayout;

    @NonNull
    Callback mCallback;

    public MenuController(@NonNull ToolbarBuilder toolbarBuilder, @NonNull Callback callback) {
        mContext = toolbarBuilder.getRoot();
        mCallback = callback;
        mNavigationView = ViewUtil.findViewByMatcher(toolbarBuilder.getDrawerLayout(), view -> (view instanceof NavigationView));
        mDrawerLayout = toolbarBuilder.getDrawerLayout();
    }

    public MenuController bind(LifecycleDelegate delegate) {
        delegate.getCallbackQueue().subscribe(next -> {
            switch (next.getState()) {
                case OnResumed:
                    mNavigationView.setNavigationItemSelectedListener(miItemSelectedImpl);
                    break;
            }
        });
        return this;
    }

    public interface Callback {
        /**
         * プラグイン詳細メニューを開く
         */
        void requestShowPlugins(MenuController self);
    }

    private final NavigationView.OnNavigationItemSelectedListener miItemSelectedImpl = (menuItem) -> {
        switch (menuItem.getItemId()) {
            case R.id.Main_Menu_Profile:
                break;
            case R.id.Main_Menu_CycleComputer:
                break;
            case R.id.Main_Menu_Commands:
                break;
            case R.id.Main_Menu_Plugins:
                mCallback.requestShowPlugins(this);
                break;
            case R.id.Main_Menu_Information:
                break;
            case R.id.Main_Menu_Gadgets:
                break;
            default:
                return false;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    };
}
