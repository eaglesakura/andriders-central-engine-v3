package com.eaglesakura.andriders.ui.navigation.menu;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.service.central.CentralService;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;
import com.eaglesakura.andriders.ui.navigation.display.DisplaySettingFragmentMain;
import com.eaglesakura.andriders.ui.navigation.extension.ExtensionFragmentMain;
import com.eaglesakura.andriders.ui.navigation.info.InformationFragmentMain;
import com.eaglesakura.andriders.ui.navigation.log.UserLogFragmentMain;
import com.eaglesakura.andriders.ui.navigation.profile.ProfileFragmentMain;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.widget.CompoundButton;

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
    CompoundButton mBootSwitch;

    @NonNull
    MenuCallback mCallback;

    public MenuController(final Context context, NavigationView view, DrawerLayout drawerLayout) {
        this.mContext = context;
        this.mNavigationView = view;
        this.mDrawerLayout = drawerLayout;
        this.mBootSwitch = (CompoundButton) mNavigationView.getHeaderView(0).findViewById(R.id.Main_Menu_BootService);
        mBootSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                CentralService.start(context);
            } else {
                CentralService.stop(context);
            }
        });
    }

    public void initialize() {
        mNavigationView.setNavigationItemSelectedListener(naviItemSelectedImpl);
    }

    public void setCallback(MenuCallback callback) {
        this.mCallback = callback;
    }

    public void onResume() {
        mBootSwitch.setChecked(CentralService.isRunning(mContext));
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
            case R.id.Main_Menu_Extensions:
                mCallback.requestChangeContent(ExtensionFragmentMain.newInstance(mContext));
                break;
            case R.id.Main_Menu_Information:
                mCallback.requestChangeContent(InformationFragmentMain.createInstance(mContext));
                break;
            default:
                return false;
        }
        mDrawerLayout.closeDrawer(Gravity.START);
        return true;
    };
}
