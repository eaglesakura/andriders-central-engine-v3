package com.eaglesakura.andriders.ui.navigation.plugin;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.CentralPluginCollection;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.ToolbarBuilder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;

/**
 * 拡張機能の設定を行う。
 */
@FragmentLayout(R.layout.system_fragment_stack)
public class PluginSettingFragmentMain extends AppNavigationFragment {

    @Inject(AppManagerProvider.class)
    PluginDataManager mPluginDataManager;

    @Nullable
    CentralPluginCollection mPlugins;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {

            int[] ICON_TABLE = {
                    R.drawable.ic_location,
                    R.drawable.ic_heart_beats,
                    R.drawable.ic_speed,
                    R.drawable.ic_single_module,
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
                PluginCategorySettingFragment fragment = new PluginCategorySettingFragment();
                fragment.setResourceId(ICON_TABLE[i], TITLE_TABLE[i], INFO_TABLE[i]);
                fragment.setCategoryName(CATEGORY_TABLE[i].getName());

                transaction.add(R.id.Content_List_Root, fragment, fragment.getClass().getName() + "_" + i).commit();
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        async(ExecuteTarget.LocalQueue, CallbackTime.CurrentForeground, (BackgroundTask<CentralPluginCollection> task) -> {
            CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task);
            try (ProgressToken token = pushProgress(R.string.Common_File_Load)) {

                CentralPluginCollection pluginCollection = mPluginDataManager.listPlugins(PluginDataManager.PluginListingMode.All, cancelCallback);

                CentralPlugin.ConnectOption option = new CentralPlugin.ConnectOption();
                pluginCollection.connect(option, cancelCallback);

                return pluginCollection;
            }
        }).completed((pluginCollection, task) -> {
            mPlugins = pluginCollection;
        }).start();

        ToolbarBuilder.from(this).title(R.string.Title_Plugin).build();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mPlugins != null) {
            CentralPluginCollection pluginCollection = mPlugins;
            mPlugins = null;
            async(ExecuteTarget.LocalQueue, CallbackTime.FireAndForget, it -> {
                pluginCollection.disconnect();
                return this;
            }).start();
        }
    }

    /**
     * 読み込まれたプラグインを保持する
     */
    @Nullable
    public CentralPluginCollection getPlugins() {
        return mPlugins;
    }
}
