package com.eaglesakura.andriders.plugin;

import com.eaglesakura.andriders.data.db.CentralSettingDatabase;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.model.plugin.ActivePluginCollection;
import com.eaglesakura.andriders.plugin.internal.PluginServerImpl;
import com.eaglesakura.andriders.system.manager.CentralSettingManager;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.lambda.CancelCallback;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * プラグイン情報のコントロールを行う
 */
public class PluginDataManager extends CentralSettingManager {
    public PluginDataManager(Context context) {
        super(context);
    }

    /**
     * Enable/Disableを切り替える
     * 排他制御は別途UI側で行う。
     *
     * @param plugin 対象プラグイン
     * @param use    有効化する場合true
     */
    public void setActive(CentralPlugin plugin, boolean use) throws AppException {
        plugin.onActive(use);
        try (CentralSettingDatabase db = open()) {
            if (use) {
                db.activePlugin(plugin.getInformation(), plugin.getPackageInfo());
            } else {
                db.disablePlugin(plugin.getPackageInfo());
            }
        }
    }

    public boolean isActive(CentralPlugin plugin) throws AppException {
        try (CentralSettingDatabase db = open()) {
            return db.isActivePlugin(plugin.getPackageInfo().serviceInfo.packageName, plugin.getPackageInfo().serviceInfo.name);
        }
    }

    /**
     * プラグインの有効情報を読み出す
     */
    ActivePluginCollection listActivePlugins() {
        try (CentralSettingDatabase db = open()) {
            return db.listPlugins();
        }
    }

    /**
     * 拡張Service一覧を取得する
     */
    List<ResolveInfo> listExtensionServices(boolean activeOnly) {
        PackageManager pm = mContext.getPackageManager();
        Intent intent = new Intent(PluginServerImpl.ACTION_ACE_EXTENSION_BIND);
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, 0);


        // Activeのみをフィルタリングする
        if (activeOnly) {
            ActivePluginCollection collection = listActivePlugins();

            Iterator<ResolveInfo> iterator = resolveInfos.iterator();
            while (iterator.hasNext()) {
                ResolveInfo info = iterator.next();
                if (!collection.isActive(info)) {
                    AppLog.system("Not Active Plugin[%s@%s]", info.serviceInfo.packageName, info.serviceInfo.name);
                    iterator.remove();
                }
            }
        }
        return resolveInfos;
    }

    public enum PluginListingMode {
        All,
        Active
    }

    /**
     * 指定した条件のPluginを全て列挙する
     *
     * @param mode 列挙モード
     */
    public CentralPluginCollection listPlugins(PluginListingMode mode, CancelCallback cancelCallback) throws TaskCanceledException, AppException {
        List<ResolveInfo> resolveInfoList = listExtensionServices(mode == PluginListingMode.Active);
        AppSupportUtil.assertNotCanceled(cancelCallback);

        List<CentralPlugin> plugins = new ArrayList<>();

        for (ResolveInfo info : resolveInfoList) {
            CentralPlugin plugin = new CentralPlugin(mContext, info);
            plugins.add(plugin);
        }

        return new CentralPluginCollection(plugins);
    }
}
