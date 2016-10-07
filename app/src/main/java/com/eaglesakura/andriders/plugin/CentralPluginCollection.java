package com.eaglesakura.andriders.plugin;

import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.CollectionUtil;

import android.support.annotation.WorkerThread;

import java.util.List;

public class CentralPluginCollection extends DataCollection<CentralPlugin> {

    public CentralPluginCollection(List<CentralPlugin> pluginList) {
        super(pluginList);
        setComparator(CentralPlugin.COMPARATOR_ASC);
    }

    /**
     * 表示設定を持つクライアントのみを列挙する
     */
    public List<CentralPlugin> listDisplayPlugins() {
        return list(plugin -> !CollectionUtil.isEmpty(plugin.getDisplayInformationList()));
    }

    /**
     * 指定したIDのプラグインを検索素r
     */
    public CentralPlugin findFromId(String id) {
        return find(client -> client.getInformation().getId().equals(id));
    }


    public DisplayKey findDisplayInformation(String pluginId, String displayId) {
        CentralPlugin client = findFromId(pluginId);
        if (client != null) {
            return client.findDisplayInformation(displayId);
        } else {
            return null;
        }
    }

    /**
     * 指定した表示内容をサポートするプラグインを検索する
     */
    public CentralPlugin find(DisplayKey key) {
        return find(plugin -> {
            List<DisplayKey> informations = plugin.getDisplayInformationList();
            if (CollectionUtil.isEmpty(informations)) {
                return false;
            }

            for (DisplayKey info : informations) {
                if (info.equals(key)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * 指定したカテゴリのクライアント一覧を返す
     */
    public List<CentralPlugin> list(Category category) {
        return list(client -> {
            // 拡張機能を列挙する
            PluginInformation information = client.getInformation();
            return category.equals(information.getCategory());
        });
    }

    /**
     * Plugin Serviceへ接続する
     */
    public void connect(CentralPlugin.ConnectOption option, CancelCallback cancelCallback) throws TaskCanceledException, AppException {
        AndroidThreadUtil.assertBackgroundThread();

        try {
            each(plugin -> {
                plugin.connect(option, cancelCallback);
            });
        } catch (Throwable e) {
            safeEach(plugin -> {
                plugin.disconnect(() -> false);
            });

            AppException.throwAppExceptionOrTaskCanceled(e);
        }
    }

    /**
     * Plugin Serviceから切断する
     */
    public void disconnect() throws AppException {
        AndroidThreadUtil.assertBackgroundThread();

        safeEach(plugin -> {
            plugin.disconnect(() -> false);
        });
    }
}
