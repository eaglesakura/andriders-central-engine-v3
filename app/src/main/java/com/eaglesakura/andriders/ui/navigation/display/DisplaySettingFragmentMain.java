package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.NavigationBaseFragment;
import com.eaglesakura.android.framework.ui.FragmentHolder;
import com.eaglesakura.android.util.AndroidThreadUtil;

import android.content.Context;
import android.support.annotation.Nullable;

/**
 * サイコンの表示内容を確定するFragment
 */
public class DisplaySettingFragmentMain extends NavigationBaseFragment implements AppTargetSelectFragment.Callback {

    FragmentHolder<AppTargetSelectFragment> mAppTargetSelectFragment =
            FragmentHolder.newInstance(this, AppTargetSelectFragment.class, R.id.Setting_Display_AppSelector_Root).bind(mLifecycleDelegate);

    FragmentHolder<DisplayLayoutSetFragment> mDisplayLayoutSetFragment =
            FragmentHolder.newInstance(this, DisplayLayoutSetFragment.class, R.id.Setting_Display_LayoutSet_Root).bind(mLifecycleDelegate);

    public DisplaySettingFragmentMain() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_setting_display_main);
    }

    /**
     * 表示対象のアプリが変更になったら、Fragmentを切り替える
     */
    @Override
    public void onApplicationSelected(AppTargetSelectFragment fragment, AppTargetSelectFragment.AppInfo selected) {
        AndroidThreadUtil.assertUIThread();
        mDisplayLayoutSetFragment.get().loadDisplayData(selected.getPackageName());
    }

    @Nullable
    @Override
    public CharSequence getTitle() {
        return getString(R.string.Main_Menu_CycleComputer);
    }

    public static DisplaySettingFragmentMain newInstance(Context context) {
        return new DisplaySettingFragmentMain();
    }
}
