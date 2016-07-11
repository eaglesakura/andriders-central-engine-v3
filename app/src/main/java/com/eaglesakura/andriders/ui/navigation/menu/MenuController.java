package com.eaglesakura.andriders.ui.navigation.menu;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.service.central.CentralService;
import com.eaglesakura.andriders.ui.navigation.NavigationBaseFragment;
import com.eaglesakura.andriders.ui.navigation.command.CommandSettingFragmentMain;
import com.eaglesakura.andriders.ui.navigation.display.DisplaySettingFragmentMain;
import com.eaglesakura.andriders.ui.navigation.gadget.GadgetSettingFragmentMain;
import com.eaglesakura.andriders.ui.navigation.info.InformationFragmentMain;
import com.eaglesakura.andriders.ui.navigation.log.UserLogFragmentMain;
import com.eaglesakura.andriders.ui.navigation.plugin.PluginSettingFragmentMain;
import com.eaglesakura.andriders.ui.navigation.profile.ProfileFragmentMain;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.android.margarine.OnClick;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.widget.Button;

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

    /**
     * セッション開始・シャットダウンボタン
     */
    @Bind(R.id.Main_Menu_Session_Boot)
    Button mServiceButton;

    boolean mInitialized;

    public MenuController(final Context context, NavigationView view, DrawerLayout drawerLayout) {
        mContext = context;
        mNavigationView = view;
        mDrawerLayout = drawerLayout;
    }

    public void setCallback(MenuCallback callback) {
        this.mCallback = callback;
    }

    public void onResume() {

        if (!mInitialized) {
            mNavigationView.setNavigationItemSelectedListener(mNaviItemSelectedImpl);

            MargarineKnife.from(mNavigationView.getHeaderView(0))
                    .to(this)
                    .bind();

            mInitialized = true;
        }

        updateServiceButton();
    }

    public void onPause() {
    }

    @OnClick(R.id.Main_Menu_Session_Boot)
    void clickSessionBoot() {
        if (CentralService.isRunning(mContext)) {
            // サービスシャットダウンする？
            onServiceShutdownCheck();
        } else {
            // サービスを起動する？
            onServiceBootCheck();
        }
    }

    /**
     * Central Serviceの状態をUIに反映させる
     */
    private void updateServiceButton() {
        if (CentralService.isRunning(mContext)) {
            mServiceButton.setText("セッション記録中");
        } else {
            mServiceButton.setText("セッション開始");
        }
    }

    private void onServiceShutdownCheck() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("セッション終了");
        builder.setMessage("走行セッションを終了しますか？");
        builder.setPositiveButton("セッション終了", (dlg, which) -> {
            CentralService.stop(mContext);
            updateServiceButton();
        });
        builder.setNegativeButton(R.string.Common_Cancel, null);
        builder.show();
    }

    private void onServiceBootCheck() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("セッション開始");
        builder.setMessage("走行セッションを開始しますか？");
        builder.setPositiveButton("セッション開始", (dlg, which) -> {
            CentralService.start(mContext);
            updateServiceButton();
        });
        builder.setNegativeButton(R.string.Common_Cancel, null);
        builder.show();
    }

    public interface MenuCallback {
        void requestChangeContent(NavigationBaseFragment fragment);
    }

    private final NavigationView.OnNavigationItemSelectedListener mNaviItemSelectedImpl = (menuItem) -> {
        switch (menuItem.getItemId()) {
            case R.id.Main_Menu_Profile:
                mCallback.requestChangeContent(ProfileFragmentMain.createInstance(mContext));
                break;
            case R.id.Main_Menu_CycleComputer:
                mCallback.requestChangeContent(DisplaySettingFragmentMain.newInstance(mContext));
                break;
            case R.id.Main_Menu_Commands:
                mCallback.requestChangeContent(new CommandSettingFragmentMain());
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
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    };
}
