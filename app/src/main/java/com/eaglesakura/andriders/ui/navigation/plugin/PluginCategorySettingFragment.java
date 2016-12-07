package com.eaglesakura.andriders.ui.navigation.plugin;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.CentralPluginCollection;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.SupportAQuery;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.saver.BundleState;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

/**
 * カテゴリごとにプラグインを列挙するFragment
 */
public class PluginCategorySettingFragment extends AppFragment {

    @BundleState
    int mTitleResId;

    @BundleState
    int mInfoResId;

    @BundleState
    int mIconResId;

    @BundleState
    String mCategoryName;

    @Bind(R.id.Extension_List_Root)
    ViewGroup mModulesRoot;

    @Inject(AppManagerProvider.class)
    PluginDataManager mPluginDataManager;

    CentralPluginCollection mCentralPluginCollection;

    public PluginCategorySettingFragment() {
        mFragmentDelegate.setLayoutId(R.layout.plugin_module);
    }

    /**
     * 各種リソースを指定する
     */
    public void setResourceId(@DrawableRes int icon, @StringRes int title, @StringRes int info) {
        this.mIconResId = icon;
        this.mTitleResId = title;
        this.mInfoResId = info;
    }

    public void setCategoryName(String categoryName) {
        this.mCategoryName = categoryName;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        SupportAQuery q = new SupportAQuery(self.getView());
        q.id(R.id.App_HeaderView_Icon).image(mIconResId);
        q.id(R.id.App_HeaderView_Title).text(mTitleResId);
        q.id(R.id.Extension_Category_Info).text(mInfoResId);
    }

    @Override
    public void onResume() {
        super.onResume();

        asyncUI(task -> {
            while (getParent(PluginSettingFragmentMain.class).getPlugins() == null) {
                task.waitTime(1);
            }
            return this;
        }).completed((result, task) -> {
            updateExtensionViews(getParent(PluginSettingFragmentMain.class).getPlugins());
        }).start();
    }

    @UiThread
    void updateExtensionViews(CentralPluginCollection pluginCollection) {
        // 取得成功したらViewに反映する
        mModulesRoot.removeAllViews();
        mCentralPluginCollection = pluginCollection;


        Category pluginCategory = Category.fromName(mCategoryName);
        List<CentralPlugin> pluginList = pluginCollection.list(pluginCategory);
        for (CentralPlugin plugin : pluginList) {

            boolean pluginActive = false;
            try {
                pluginActive = mPluginDataManager.isActive(plugin);
            } catch (Exception e) {
            }

            // クライアント表示を追加する
            View card = View.inflate(getActivity(), R.layout.plugin_module_row, null);
            AQuery q = new AQuery(card);
            q.id(R.id.Extension_Module_Icon).image(plugin.loadIcon());
            // チェック有無判定
            q.id(R.id.Extension_Module_Switch)
                    .text(plugin.getName())
                    .checked(pluginActive)
                    .checkedChange((button, isChecked) -> {
                        activate(plugin, isChecked);
                    });

            PluginInformation information = plugin.getInformation();
            if (information != null) {
                // TODO 説明テキスト設定
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mModulesRoot.addView(card, params);
        }
    }

    /**
     * プラグインのON/OFF制御を行う
     *
     * @param plugin 対象プラグイン
     */
    @UiThread
    void activate(CentralPlugin plugin, boolean isChecked) {
        asyncUI(task -> {
            Category pluginCategory = Category.fromName(mCategoryName);
            List<CentralPlugin> pluginList = mCentralPluginCollection.list(pluginCategory);

            try (ProgressToken token = pushProgress(R.string.Word_Common_Working)) {
                if (isChecked && pluginCategory.hasAttribute(Category.ATTRIBUTE_SINGLE_SELECT)) {
                    // 1つしか選択できないのなら、一旦全てを外す
                    for (CentralPlugin p : pluginList) {
                        mPluginDataManager.setActive(p, false);
                    }
                }

                // プラグインのチェック状態を更新する
                mPluginDataManager.setActive(plugin, isChecked);
            }
            return this;
        }).failed((error, task) -> {
            AppLog.printStackTrace(error);
            AppDialogBuilder.newError(getContext(), error)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(mLifecycleDelegate);
        }).start();
    }

}
