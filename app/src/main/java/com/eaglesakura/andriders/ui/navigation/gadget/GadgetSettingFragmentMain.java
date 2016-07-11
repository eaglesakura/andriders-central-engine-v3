package com.eaglesakura.andriders.ui.navigation.gadget;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.NavigationBaseFragment;
import com.eaglesakura.android.framework.ui.FragmentHolder;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * 接続する機材の設定を行う
 */
public class GadgetSettingFragmentMain extends NavigationBaseFragment {

    FragmentHolder<GadgetSettingFragment> mGadgetSettingFragment = FragmentHolder.newInstance(this, GadgetSettingFragment.class, R.id.Content_List_Root);

    public GadgetSettingFragmentMain() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_simiple_main);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGadgetSettingFragment.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        mGadgetSettingFragment.onResume();
    }

    @Nullable
    @Override
    public CharSequence getTitle() {
        return getString(R.string.Main_Menu_Gadgets);
    }

    public static GadgetSettingFragmentMain newInstance(Context context) {
        return new GadgetSettingFragmentMain();
    }
}
