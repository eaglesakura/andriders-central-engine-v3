package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.model.display.DisplayLayoutGroup;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.CentralPluginCollection;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.lambda.CancelCallback;

import android.content.Context;

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

    PluginDataManager mPluginDataManager;

    public DisplayLayoutController(Context context) {
        mContext = context;
    }

    /**
     * ディスプレイ情報をすべてロードする
     */
    public void load(CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        CentralPluginCollection plugins = mPluginDataManager.listPlugins(PluginDataManager.PluginListingMode.Active, cancelCallback);
        try {
            plugins.connect(new CentralPlugin.ConnectOption(), cancelCallback);

            for (CentralPlugin plugin : plugins.listDisplayPlugins()) {
                plugin.listDisplayKeys();
            }
        } finally {
            plugins.disconnect();
        }
    }
}
