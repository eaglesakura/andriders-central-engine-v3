package com.eaglesakura.andriders.ui.navigation.gadget;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.PluginManager;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;
import com.eaglesakura.andriders.ui.navigation.plugin.PluginCategorySettingFragment;
import com.eaglesakura.android.framework.ui.FragmentHolder;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.SubscribeTarget;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

/**
 * 接続する機材の設定を行う
 */
public class GadgetSettingFragmentMain extends BaseNavigationFragment {

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

    public static GadgetSettingFragmentMain newInstance(Context context) {
        return new GadgetSettingFragmentMain();
    }
}
