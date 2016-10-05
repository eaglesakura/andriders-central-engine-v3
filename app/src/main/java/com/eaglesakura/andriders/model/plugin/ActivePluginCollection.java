package com.eaglesakura.andriders.model.plugin;

import com.eaglesakura.collection.DataCollection;

import android.content.pm.ResolveInfo;

import java.util.List;

public class ActivePluginCollection extends DataCollection<ActivePlugin> {

    public ActivePluginCollection(List<ActivePlugin> dataList) {
        super(dataList);
    }

    /**
     * 指定したプラグインが有効であればtrueを返す
     */
    public boolean isActive(String uniqueId) {
        return find(it -> it.getUniqueId().equals(uniqueId)) != null;

    }

    /**
     * 指定されたプラグインが有効であればtrue
     */
    public boolean isActive(ResolveInfo info) {
        return find(it ->
                it.getPackageName().equals(info.serviceInfo.packageName) &&
                        it.getClassName().equals(info.serviceInfo.name)) != null;
    }
}
