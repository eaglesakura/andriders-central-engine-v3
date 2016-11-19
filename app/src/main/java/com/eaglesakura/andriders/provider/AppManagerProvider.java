package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.data.display.DisplayLayoutManager;
import com.eaglesakura.andriders.data.sensor.SensorDataManager;
import com.eaglesakura.andriders.plugin.CommandDataManager;
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

    @Provide
    public CommandDataManager provideCommandDataManager() {
        return new CommandDataManager(getApplication());
    }

    @Provide
    public SensorDataManager provideSensorDataManager() {
        return new SensorDataManager(getApplication());
    }

    @Provide
    public DisplayLayoutManager provideDisplayLayoutManager() {
        return new DisplayLayoutManager(getApplication());
    }

    @Override
    public void onInjectCompleted(Object inject) {

    }
}
