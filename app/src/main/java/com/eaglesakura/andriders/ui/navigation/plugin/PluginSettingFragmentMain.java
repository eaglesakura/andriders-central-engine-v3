package com.eaglesakura.andriders.ui.navigation.plugin;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.CentralPluginCollection;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;
import com.eaglesakura.sloth.ui.progress.ProgressToken;
import com.eaglesakura.sloth.view.builder.ToolbarBuilder;

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
                    R.string.Title_PluginCategory_Location,
                    R.string.Title_PluginCategory_Heartrate,
                    R.string.Title_PluginCategory_SpeedAndCadence,
                    R.string.Title_PluginCategory_Other,
            };
            int[] INFO_TABLE = {
                    R.string.Message_PluginCategory_Location,
                    R.string.Message_PluginCategory_Heartrate,
                    R.string.Message_PluginCategory_SpeedAndCadence,
                    R.string.Message_PluginCategory_Other,
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
        getFragmentLifecycle().async(ExecuteTarget.LocalQueue, CallbackTime.CurrentForeground, (BackgroundTask<CentralPluginCollection> task) -> {
            SupportCancelCallbackBuilder.CancelChecker checker = SupportCancelCallbackBuilder.from(task).build();
            try (ProgressToken token = pushProgress(R.string.Word_Common_DataLoad)) {
                CentralPluginCollection pluginCollection = mPluginDataManager.listPlugins(PluginDataManager.PluginListingMode.All, checker);

                CentralPlugin.ConnectOption option = new CentralPlugin.ConnectOption();
                pluginCollection.connect(option, checker);

                return pluginCollection;
            }
        }).completed((pluginCollection, task) -> {
            mPlugins = pluginCollection;
        }).failed((error, task) -> {
            AppLog.report(error);
            AppDialogBuilder.newError(getContext(), error)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getFragmentLifecycle());
        }).start();

        ToolbarBuilder.from(this).title(R.string.Title_Plugin).build();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mPlugins != null) {
            CentralPluginCollection pluginCollection = mPlugins;
            mPlugins = null;
            getFragmentLifecycle().async(ExecuteTarget.LocalQueue, CallbackTime.FireAndForget, it -> {
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
