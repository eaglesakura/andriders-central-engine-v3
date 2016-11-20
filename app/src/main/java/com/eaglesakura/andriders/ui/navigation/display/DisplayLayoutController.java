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
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public DisplayLayoutGroup getLayoutGroup(String packageName) {
        synchronized (mLayouts) {
            if (StringUtil.isEmpty(packageName)) {
                packageName = DisplayLayout.PACKAGE_NAME_DEFAULT;
            }

            DisplayLayoutGroup result = mLayouts.get(packageName);
            if (result == null) {
                result = new DisplayLayoutGroup(new ArrayList<>(), packageName);
                // デフォルト構成を生成する
                for (int h = 0; h < DisplayLayoutManager.MAX_VERTICAL_SLOTS; ++h) {
                    for (int v = 0; v < DisplayLayoutManager.MAX_HORIZONTAL_SLOTS; ++v) {
                        DisplayLayout layout = new DisplayLayout.Builder(DisplayLayout.getSlotId(h, v))
                                .application(packageName)
                                .build();
                        result.insertOrReplace(layout);
                    }
                }
                mLayouts.put(packageName, result);
            }
            return result;
        }
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
        } finally {
            plugins.disconnect();
        }

        // すべてのレイアウトを列挙し、グルーピングする
        DisplayLayoutCollection allLayout = mDisplayLayoutManager.list();
        for (DisplayLayout layout : allLayout.getSource()) {
            DisplayLayoutGroup group = getLayoutGroup(layout.getAppPackageName());
            group.insertOrReplace(layout);
        }
    }

    /**
     * データ保存を行う
     */
    public void commit() throws AppException {
        synchronized (mLayouts) {
            List<DisplayLayout> buffer = new ArrayList<>(mLayouts.size() * DisplayLayoutManager.MAX_VERTICAL_SLOTS * DisplayLayoutManager.MAX_HORIZONTAL_SLOTS);
            for (DisplayLayoutGroup group : mLayouts.values()) {
                buffer.addAll(group.getSource());
            }
            mDisplayLayoutManager.update(buffer);
        }
    }

    /**
     * 指定したアプリパッケージ構成を削除する
     *
     * @param packageName アプリパッケージ名
     */
    public void remove(@NonNull String packageName) throws AppException {
        if (StringUtil.isEmpty(packageName)) {
            throw new NullPointerException("packageName == null");
        }
        synchronized (mLayouts) {
            mDisplayLayoutManager.removeAll(packageName);   // DB削除
            mLayouts.remove(packageName);   // メモリキャッシュ削除
        }
    }

    public interface Holder {
        DisplayLayoutController getDisplayLayoutController();
    }
}
