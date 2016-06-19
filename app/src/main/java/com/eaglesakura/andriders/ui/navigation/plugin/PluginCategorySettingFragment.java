package com.eaglesakura.andriders.ui.navigation.plugin;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.db.plugin.PluginDatabase;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.PluginConnector;
import com.eaglesakura.andriders.plugin.PluginManager;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.SupportAQuery;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.bundle.BundleState;
import com.eaglesakura.util.Util;

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
public class PluginCategorySettingFragment extends AppBaseFragment {

    @BundleState
    int mTitleResId;

    @BundleState
    int mInfoResId;

    @BundleState
    int mIconResId;

    @BundleState
    String mCategoryName;

    @Bind(R.id.Extension_List_Root)
    ViewGroup modulesRoot;

    /**
     * 親は確定している
     */
    PluginSettingFragmentMain mParent;

    public PluginCategorySettingFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_plugin_modules);
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

        mParent = (PluginSettingFragmentMain) getParentFragment();
        if (mParent == null) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        SupportAQuery q = new SupportAQuery(this);
        q.id(R.id.App_HeaderView_Icon).image(mIconResId);
        q.id(R.id.App_HeaderView_Title).text(mTitleResId);
        q.id(R.id.Extension_Category_Info).text(mInfoResId);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateExtensionViews();
    }

    @UiThread
    void updateExtensionViews() {
        asyncUI((RxTask<PluginManager> it) -> {
            while (!it.isCanceled()) {
                PluginManager manager = mParent.getClientManager();
                if (manager != null) {
                    return manager;
                }
                Util.sleep(1);
            }

            throw new IllegalStateException();
        }).completed((manager, task) -> {
            // 取得成功したらViewに反映する
            modulesRoot.removeAllViews();

            List<PluginConnector> clients = manager.listClients(Category.fromName(mCategoryName));
            for (PluginConnector client : clients) {
                // クライアント表示を追加する
                addClientSetting(client);
            }
        }).start();
    }

    /**
     * TODO クライアントのViewを構築する
     */
    @UiThread
    private void addClientSetting(final PluginConnector client) {
        View card = View.inflate(getActivity(), R.layout.card_extension_module, null);
        AQuery q = new AQuery(card);
        q.id(R.id.Extension_Module_Icon).image(client.loadIcon());
        // チェック有無判定
        q.id(R.id.Extension_Module_Switch)
                .text(client.getName())
                .checked(client.isActive())
                .checkedChange((button, isChecked) -> {
                    client.setEnable(isChecked);
                });

        PluginInformation information = client.getInformation();
        if (information != null) {
            // TODO 説明テキスト設定
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        modulesRoot.addView(card, params);
    }

    @UiThread
    void commitEnableChanged(PluginConnector plugin, boolean enabled) {
        asyncUI(task -> {
            PluginDatabase db = new PluginDatabase(getActivity());
            try {
                db.openWritable();

                Category category = Category.fromName(mCategoryName);

                if (category.hasAttribute(Category.ATTRIBUTE_SINGLE_SELECT)) {
                    // 属性を指定する
                }
            } finally {
                db.close();
            }

            return this;
        }).start();
    }

}
