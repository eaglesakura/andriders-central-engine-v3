package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.android.framework.provider.ContextProvider;
import com.eaglesakura.android.garnet.Provide;

/**
 * Manager系の依存管理を行う
 */
public class AppManagerProvider extends ContextProvider {

    @Override
    public void onDependsCompleted(Object inject) {

    }

    @Provide
    public PluginDataManager providePluginDataManager() {
        return new PluginDataManager(getApplication());
    }

    @Provide
    public CentralLogManager provideCentralLogManager() {
        return new CentralLogManager(getApplication());
    }

    @Override
    public void onInjectCompleted(Object inject) {

    }
}
