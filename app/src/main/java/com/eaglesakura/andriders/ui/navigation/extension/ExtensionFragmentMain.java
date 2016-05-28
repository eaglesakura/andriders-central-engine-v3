package com.eaglesakura.andriders.ui.navigation.extension;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.plugin.ExtensionClientManager;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;
import com.eaglesakura.andriders.ui.navigation.profile.GadgetSettingFragment;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.SubscribeTarget;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

/**
 * 拡張機能の設定を行う。
 */
public class ExtensionFragmentMain extends BaseNavigationFragment {

    ExtensionClientManager mClientManager;

    public ExtensionFragmentMain() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_simiple_main);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {

            {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                GadgetSettingFragment fragment = new GadgetSettingFragment();
                transaction.add(R.id.Content_List_Root, fragment, fragment.getClass().getName()).commit();
            }

            int[] ICON_TABLE = {
                    R.drawable.ic_launcher,
                    R.drawable.ic_launcher,
                    R.drawable.ic_launcher,
                    R.drawable.ic_launcher,
            };
            int[] TITLE_TABLE = {
                    R.string.Extension_Category_Location,
                    R.string.Extension_Category_Heartrate,
                    R.string.Extension_Category_SpeedAndCadence,
                    R.string.Extension_Category_Other,
            };
            int[] INFO_TABLE = {
                    R.string.Extension_Category_Location_Information,
                    R.string.Extension_Category_Heartrate_Information,
                    R.string.Extension_Category_SpeedAndCadence_Information,
                    R.string.Extension_Category_Other_Information,
            };

            Category[] CATEGORY_TABLE = {
                    Category.CATEGORY_LOCATION,
                    Category.CATEGORY_HEARTRATEMONITOR,
                    Category.CATEGORY_SPEED_AND_CADENCE,
                    Category.CATEGORY_OTHERS,
            };

            for (int i = 0; i < ICON_TABLE.length; ++i) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                ExtensionModuleSettingFragment fragment = new ExtensionModuleSettingFragment();
                fragment.setResourceId(ICON_TABLE[i], TITLE_TABLE[i], INFO_TABLE[i]);
                fragment.setCategoryName(CATEGORY_TABLE[i].getName());

                transaction.add(R.id.Content_List_Root, fragment, fragment.getClass().getName() + "_" + i).commit();
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        async(SubscribeTarget.Pipeline, ObserveTarget.CurrentForeground, (RxTask<ExtensionClientManager> task) -> {
            try {
                pushProgress(R.string.Common_File_Load);

                ExtensionClientManager clientManager = new ExtensionClientManager(getActivity());
                clientManager.connect(ExtensionClientManager.ConnectMode.All);

                return clientManager;
            } finally {
                popProgress();
            }
        }).completed((it, task) -> {
            mClientManager = it;
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();

        async(SubscribeTarget.Pipeline, ObserveTarget.FireAndForget, it -> {
            if (mClientManager != null) {
                mClientManager.disconnect();
                mClientManager = null;
            }
            return this;
        }).start();
    }

    public ExtensionClientManager getClientManager() {
        return mClientManager;
    }

    public static ExtensionFragmentMain newInstance(Context context) {
        return new ExtensionFragmentMain();
    }
}
