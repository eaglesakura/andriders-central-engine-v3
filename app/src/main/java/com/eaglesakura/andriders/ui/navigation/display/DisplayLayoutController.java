package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.data.display.DisplayLayoutManager;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.model.display.DisplayLayoutCollection;
import com.eaglesakura.andriders.model.display.DisplayLayoutGroup;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.CentralPluginCollection;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.StringUtil;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * レイアウトデータ管理
 */
public class DisplayLayoutController {
    Context mContext;

    /**
     * キャッシュされたレイアウト情報
     */
    Map<String, DisplayLayoutGroup> mLayouts = new HashMap<>();

    /**
     * 表示用UIを持ったプラグイン一覧
     */
    DataCollection<CentralPlugin> mDisplayPlugins;

    @Inject(AppManagerProvider.class)
    DisplayLayoutManager mDisplayLayoutManager;

    public DisplayLayoutController(Context context) {
        mContext = context;

        Garnet.create(this)
                .depend(Context.class, context)
                .inject();
    }

    /**
     * 管理グループを取得する
     *
     * @param packageName パッケージ名
     */
    public synchronized DisplayLayoutGroup getLayoutGroup(String packageName) {
        if (StringUtil.isEmpty(packageName)) {
            packageName = DisplayLayout.PACKAGE_NAME_DEFAULT;
        }

        DisplayLayoutGroup result = mLayouts.get(packageName);
        if (result == null) {
            result = new DisplayLayoutGroup(new ArrayList<>(), packageName);
            mLayouts.put(packageName, result);
        }

        return result;
    }

    /**
     * ディスプレイ情報をすべてロードしてキャッシュ化する
     */
    public void load(CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        PluginDataManager pluginDataManager = Garnet.instance(AppManagerProvider.class, PluginDataManager.class);

        CentralPluginCollection plugins = pluginDataManager.listPlugins(PluginDataManager.PluginListingMode.Active, cancelCallback);
        try {
            plugins.connect(new CentralPlugin.ConnectOption(), cancelCallback);

            mDisplayPlugins = new DataCollection<>(plugins.listDisplayPlugins());
            for (CentralPlugin plugin : mDisplayPlugins.getSource()) {
                PluginInformation information = plugin.getInformation();
                AppLog.plugin("DisplayPlugin id[%s] category[%s]", information.getId(), information.getCategory());
            }

            // すべてのレイアウトを列挙し、グルーピングする
            DisplayLayoutCollection allLayout = mDisplayLayoutManager.list();
            for (DisplayLayout layout : allLayout.getSource()) {
                DisplayLayoutGroup group = getLayoutGroup(layout.getAppPackageName());
                group.insertOrReplace(layout);
            }
        } finally {
            plugins.disconnect();
        }
    }

    public interface Holder {
        DisplayLayoutController getDisplayLayoutController();
    }
}
