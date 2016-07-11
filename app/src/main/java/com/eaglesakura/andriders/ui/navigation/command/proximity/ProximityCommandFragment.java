package com.eaglesakura.andriders.ui.navigation.command.proximity;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.framework.delegate.fragment.IFragmentPagerTitle;

/**
 * 近接コマンド設定クラス
 */
public class ProximityCommandFragment extends AppBaseFragment implements IFragmentPagerTitle {
    public ProximityCommandFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_command_proximity);
    }

    @Override
    public CharSequence getTitle() {
        return "近接";
    }
}
