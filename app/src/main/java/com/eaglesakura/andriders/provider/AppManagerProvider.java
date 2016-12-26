package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.data.display.DisplayLayoutManager;
import com.eaglesakura.andriders.data.migration.DataMigrationManager;
import com.eaglesakura.andriders.data.sensor.SensorDataManager;
import com.eaglesakura.andriders.gen.prop.UserProfiles;
import com.eaglesakura.andriders.plugin.CommandDataManager;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.provider.ContextProvider;
import com.eaglesakura.android.garnet.Depend;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Provide;

/**
 * Manager系の依存管理を行う
 */
public class AppManagerProvider extends ContextProvider {

    UserProfiles mUserProfiles;

    @Override
    public void onDependsCompleted(Object inject) {

    }

    @Depend
    public void setUserProfiles(UserProfiles userProfiles) {
        mUserProfiles = userProfiles;
    }

    @Provide
    public PluginDataManager providePluginDataManager() {
        if (mUserProfiles == null) {
            AppLog.system("Plugin UserProfile from AppSettings");
            mUserProfiles = Garnet.instance(AppContextProvider.class, AppSettings.class).getUserProfiles();
        } else {
            AppLog.system("Plugin UserProfile from Depend");
        }
        return new PluginDataManager(getApplication(), mUserProfiles);
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

    @Provide
    public DataMigrationManager provideMigrationManager() {
        return new DataMigrationManager(getApplication());
    }

    @Override
    public void onInjectCompleted(Object inject) {
    }
}
