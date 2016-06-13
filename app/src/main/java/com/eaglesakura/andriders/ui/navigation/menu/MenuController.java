package com.eaglesakura.andriders.ui.navigation.menu;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;
import com.eaglesakura.andriders.ui.navigation.display.DisplaySettingFragmentMain;
import com.eaglesakura.andriders.ui.navigation.gadget.GadgetSettingFragmentMain;
import com.eaglesakura.andriders.ui.navigation.plugin.PluginSettingFragmentMain;
import com.eaglesakura.andriders.ui.navigation.info.InformationFragmentMain;
import com.eaglesakura.andriders.ui.navigation.log.UserLogFragmentMain;
import com.eaglesakura.andriders.ui.navigation.profile.ProfileFragmentMain;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;

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
    MenuCallback mCallback;

    public MenuController(final Context context, NavigationView view, DrawerLayout drawerLayout) {
        mContext = context;
        mNavigationView = view;
        mDrawerLayout = drawerLayout;
    }

    public void initialize() {
        mNavigationView.setNavigationItemSelectedListener(naviItemSelectedImpl);
    }

    public void setCallback(MenuCallback callback) {
        this.mCallback = callback;
    }

    public void onResume() {
    }

    public void onPause() {

    }

    public interface MenuCallback {
        void requestChangeContent(BaseNavigationFragment fragment);
    }

    private final NavigationView.OnNavigationItemSelectedListener naviItemSelectedImpl = (menuItem) -> {
        switch (menuItem.getItemId()) {
            case R.id.Main_Menu_Profile:
                mCallback.requestChangeContent(ProfileFragmentMain.createInstance(mContext));
                break;
            case R.id.Main_Menu_CycleComputer:
                mCallback.requestChangeContent(DisplaySettingFragmentMain.newInstance(mContext));
                break;
            case R.id.Main_Menu_UserLog:
                mCallback.requestChangeContent(UserLogFragmentMain.newInstance(mContext));
                break;
            case R.id.Main_Menu_Plugins:
                mCallback.requestChangeContent(PluginSettingFragmentMain.newInstance(mContext));
                break;
            case R.id.Main_Menu_Information:
                mCallback.requestChangeContent(InformationFragmentMain.newInstance(mContext));
                break;
            case R.id.Main_Menu_Gadgets:
                mCallback.requestChangeContent(GadgetSettingFragmentMain.newInstance(mContext));
                break;
            default:
                return false;
        }
        mDrawerLayout.closeDrawer(Gravity.START);
        return true;
    };
}
