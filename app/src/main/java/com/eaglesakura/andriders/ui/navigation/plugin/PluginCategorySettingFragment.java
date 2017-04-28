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
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.sloth.annotation.BindInterface;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.ui.progress.ProgressToken;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

/**
 * カテゴリごとにプラグインを列挙するFragment
 */
@FragmentLayout(R.layout.plugin_module)
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

    /**
     * 親Fragmentはこれが必須である
     */
    @BindInterface
    PluginSettingFragmentMain mParent;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        AQuery q = new AQuery(view);
        q.id(R.id.App_HeaderView_Icon).image(mIconResId);
        q.id(R.id.App_HeaderView_Title).text(mTitleResId);
        q.id(R.id.Extension_Category_Info).text(mInfoResId);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        asyncQueue(task -> {
            while (mParent.getPlugins() == null) {
                task.waitTime(1);
            }
            return this;
        }).completed((result, task) -> {
            updatePluginViews(mParent.getPlugins());
        }).start();
    }

    @UiThread
    void updatePluginViews(CentralPluginCollection pluginCollection) {
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
            q.id(R.id.Item_Summary).text(information.getSummary());
            // 設定画面のリンク
            q.id(R.id.Button_Setting).visibility(information.hasSetting() ? View.VISIBLE : View.GONE).clicked(view -> {
                showPluginSetting(plugin);
            });
            // SDKバージョン表記
            q.id(R.id.Item_AceSDKVersion).text("SDK v" + information.getSdkVersion());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mModulesRoot.addView(card, params);
        }
    }

    @UiThread
    void showPluginSetting(CentralPlugin plugin) {
        if (!plugin.startSettings()) {
            AppDialogBuilder.newAlert(getContext(), "正常にプラグインの設定画面を開けませんでした。")
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getLifecycle());
        }
    }

    /**
     * プラグインのON/OFF制御を行う
     *
     * @param plugin 対象プラグイン
     */
    @UiThread
    void activate(CentralPlugin plugin, boolean isChecked) {
        asyncQueue(task -> {
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
            AppLog.report(error);
            AppDialogBuilder.newError(getContext(), error)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getLifecycle());
        }).start();
    }

}
