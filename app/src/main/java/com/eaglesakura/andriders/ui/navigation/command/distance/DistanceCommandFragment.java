package com.eaglesakura.andriders.ui.navigation.command.distance;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.framework.delegate.fragment.IFragmentPagerTitle;

public class DistanceCommandFragment extends AppBaseFragment implements IFragmentPagerTitle {

    public DistanceCommandFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_command_list);
    }

    @Override
    public CharSequence getTitle() {
        return "距離";
    }
}
