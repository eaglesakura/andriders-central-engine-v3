package com.eaglesakura.andriders.db.plugin;

import com.eaglesakura.lambda.Matcher1;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PluginCollection {

    @NonNull
    final List<ActivePlugin> mPlugins;

    PluginCollection(@NonNull List<ActivePlugin> plugins) {
        mPlugins = plugins;
    }

    /**
     * 指定条件のプラグインを持っていればtrue
     */
    public boolean hasPlugin(Matcher1<ActivePlugin> matcher) {
        try {
            for (ActivePlugin plugin : mPlugins) {
                if (matcher.match(plugin)) {
                    return true;
                }
            }
            return false;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public List<ActivePlugin> list(Matcher1<ActivePlugin> matcher) {
        List<ActivePlugin> result = new ArrayList<>();
        for (ActivePlugin plugin : mPlugins) {
            try {
                if (matcher.match(plugin)) {
                    result.add(plugin);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
