package com.eaglesakura.andriders.ui.navigation.menu;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.service.central.CentralService;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;
import com.eaglesakura.andriders.ui.navigation.display.DisplaySettingFragmentMain;
import com.eaglesakura.andriders.ui.navigation.extension.ExtensionFragmentMain;
import com.eaglesakura.andriders.ui.navigation.info.InformationFragmentMain;
import com.eaglesakura.andriders.ui.navigation.profile.ProfileFragmentMain;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.CompoundButton;

/**
 * メニュー内容を動的に制御するクラス。
 * <p/>
 * 将来的に、拡張モジュールごとにメニューを割り当てることが考えられる。
 * そのため、menuリソースを使わず、プログラム側でコントロールを行う。
 */
public class MenuController {
    final Context context;

    final NavigationView navigationView;

    final DrawerLayout drawerLayout;

    CompoundButton bootSwitch;

    MenuCallback callback;

    public MenuController(final Context context, NavigationView view, DrawerLayout drawerLayout) {
        this.context = context;
        this.navigationView = view;
        this.drawerLayout = drawerLayout;
        this.bootSwitch = (CompoundButton) navigationView.getHeaderView(0).findViewById(R.id.Main_Menu_BootService);
        bootSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    CentralService.start(context);
                } else {
                    CentralService.stop(context);
                }
            }
        });
    }

    public void initialize() {
        navigationView.setNavigationItemSelectedListener(naviItemSelectedImpl);
    }

    public void setCallback(MenuCallback callback) {
        this.callback = callback;
    }

    public void onResume() {
        bootSwitch.setChecked(CentralService.isRunning(context));
    }

    public void onPause() {

    }

    public interface MenuCallback {
        void requestChangeContent(BaseNavigationFragment fragment);
    }

    private final NavigationView.OnNavigationItemSelectedListener naviItemSelectedImpl = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.Main_Menu_Profile:
                    callback.requestChangeContent(ProfileFragmentMain.createInstance(context));
                    break;
                case R.id.Main_Menu_CycleComputer:
                    callback.requestChangeContent(DisplaySettingFragmentMain.createInstance(context));
                    break;
                case R.id.Main_Menu_Activity:
                    break;
                case R.id.Main_Menu_Extensions:
                    callback.requestChangeContent(ExtensionFragmentMain.createInstance(context));
                    break;
                case R.id.Main_Menu_Information:
                    callback.requestChangeContent(InformationFragmentMain.createInstance(context));
                    break;
                default:
                    return false;
            }
            drawerLayout.closeDrawer(Gravity.START);
            return true;
        }
    };
}
