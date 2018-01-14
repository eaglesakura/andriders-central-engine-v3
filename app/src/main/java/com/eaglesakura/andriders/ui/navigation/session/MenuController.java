package com.eaglesakura.andriders.ui.navigation.session;

import com.eaglesakura.andriders.R;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.sloth.app.lifecycle.Lifecycle;
import com.eaglesakura.sloth.view.builder.ToolbarBuilder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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

    public MenuController bind(Lifecycle delegate) {
        delegate.subscribe(next -> {
            switch (next.getState()) {
                case OnResume:
                    mNavigationView.setNavigationItemSelectedListener(miItemSelectedImpl);
                    break;
            }
        });
        return this;
    }

    public interface Callback {
        /**
         * プロファイルメニューを開く
         */
        void requestShowProfile(MenuController self);

        /**
         * レイアウト編集画面を開く
         */
        void requestShowDisplayLayout(MenuController self);

        /**
         * プラグイン詳細メニューを開く
         */
        void requestShowPlugins(MenuController self);

        /**
         * Info画面を開く
         */
        void requestShowInformation(MenuController self);

        /**
         * コマンド詳細メニューを開く
         */
        void requestShowCommands(MenuController self);

        /**
         * ユーザーログを開く
         */
        void requestShowLogs(MenuController self);

        /**
         * センサーメニューを開く
         */
        void requestShowSensorDevices(MenuController self);
    }

    private final NavigationView.OnNavigationItemSelectedListener miItemSelectedImpl = menuItem -> {
        switch (menuItem.getItemId()) {
            case R.id.Menu_Profile:
                mCallback.requestShowProfile(this);
                break;
            case R.id.Menu_CycleComputer:
                mCallback.requestShowDisplayLayout(this);
                break;
            case R.id.Menu_Commands:
                mCallback.requestShowCommands(this);
                break;
            case R.id.Menu_Plugins:
                mCallback.requestShowPlugins(this);
                break;
            case R.id.Menu_Information:
                mCallback.requestShowInformation(this);
                break;
            case R.id.Menu_Log:
                mCallback.requestShowLogs(this);
                break;
            case R.id.Menu_Gadgets:
                mCallback.requestShowSensorDevices(this);
                break;
            default:
                return false;
        }
//        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    };
}
