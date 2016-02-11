package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;
import com.eaglesakura.android.util.AndroidThreadUtil;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

/**
 * サイコンの表示内容を確定するFragment
 */
public class DisplaySettingFragmentMain extends BaseNavigationFragment implements AppTargetSelectFragment.Callback {
    public DisplaySettingFragmentMain() {
        requestInjection(R.layout.fragment_setting_display_main);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            {
                AppTargetSelectFragment fragment = new AppTargetSelectFragment();
                transaction.replace(R.id.Setting_Display_AppSelector_Root, fragment, fragment.createSimpleTag());
            }
            {
                DisplayLayoutSetFragment fragment = new DisplayLayoutSetFragment();
                transaction.replace(R.id.Setting_Display_LayoutSet_Root, fragment, fragment.createSimpleTag());
            }
            transaction.commit();
        }
    }

    /**
     * 表示対象のアプリが変更になったら、Fragmentを切り替える
     */
    @Override
    public void onApplicationSelected(AppTargetSelectFragment fragment, AppTargetSelectFragment.AppInfo selected) {
        AndroidThreadUtil.assertUIThread();
    }

    public static DisplaySettingFragmentMain createInstance(Context context) {
        return new DisplaySettingFragmentMain();
    }
}
