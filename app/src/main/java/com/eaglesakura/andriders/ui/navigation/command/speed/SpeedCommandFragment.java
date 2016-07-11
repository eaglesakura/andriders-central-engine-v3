package com.eaglesakura.andriders.ui.navigation.command.speed;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.framework.delegate.fragment.IFragmentPagerTitle;

public class SpeedCommandFragment extends AppBaseFragment implements IFragmentPagerTitle {

    public SpeedCommandFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_command_list);
    }

    @Override
    public CharSequence getTitle() {
        return "スピード";
    }
}
