package com.eaglesakura.andriders.ui.navigation.extension;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.computer.extension.client.ExtensionClient;
import com.eaglesakura.andriders.computer.extension.client.ExtensionClientManager;
import com.eaglesakura.andriders.extension.ExtensionCategory;
import com.eaglesakura.andriders.extension.ExtensionInformation;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.ui.SupportAQuery;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.util.Util;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import java.util.List;

import icepick.State;

public class ExtensionModuleSettingFragment extends AppBaseFragment {

    @State
    int titleResId;

    @State
    int infoResId;

    @State
    int iconResId;

    @State
    String categoryName;

    @Bind(R.id.Extension_List_Root)
    ViewGroup modulesRoot;

    public ExtensionModuleSettingFragment() {
        requestInjection(R.layout.fragment_extension_modules);
    }

    /**
     * 各種リソースを指定する
     */
    public void setResourceId(@DrawableRes int icon, @StringRes int title, @StringRes int info) {
        this.iconResId = icon;
        this.titleResId = title;
        this.infoResId = info;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    ExtensionFragmentMain parent;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        parent = (ExtensionFragmentMain) getParentFragment();
        if (parent == null) {
            throw new IllegalStateException();
        }
    }

    @Override
    protected void onAfterViews() {
        super.onAfterViews();

        SupportAQuery q = new SupportAQuery(this);
        q.id(R.id.Extension_Category_Icon).image(iconResId);
        q.id(R.id.Extension_Category_Name).text(titleResId);
        q.id(R.id.Extension_Category_Info).text(infoResId);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateExtensionViews();
    }

    void updateExtensionViews() {
        asyncUI((RxTask<ExtensionClientManager> it) -> {
            while (!it.isCanceled()) {
                ExtensionClientManager manager = parent.getClientManager();
                if (manager != null) {
                    return manager;
                }
                Util.sleep(1);
            }

            throw new IllegalStateException();
        }).completed((manager, task) -> {
            // 取得成功したらViewに反映する
            modulesRoot.removeAllViews();

            List<ExtensionClient> clients = manager.listClients(ExtensionCategory.fromName(categoryName));
            for (ExtensionClient client : clients) {
                // クライアント表示を追加する
                addClientSetting(client);
            }
        }).start();
    }

    /**
     * TODO クライアントのViewを構築する
     */
    void addClientSetting(final ExtensionClient client) {
        View card = View.inflate(getActivity(), R.layout.card_extension_module, null);
        AQuery q = new AQuery(card);
        q.id(R.id.Extension_Module_Icon).image(client.loadIcon());
        q.id(R.id.Extension_Module_Switch).text(client.getName()).checkedChange(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                client.setEnable(isChecked);
            }
        });

        ExtensionInformation information = client.getInformation();
        if (information != null) {
            // TODO 説明テキスト設定
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        modulesRoot.addView(card, params);
    }

}
