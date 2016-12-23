package com.eaglesakura.andriders.data.migration;

import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.model.display.DisplayLayoutGroup;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.CentralPluginCollection;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.plugin.service.CentralInterfacePluginService;
import com.eaglesakura.andriders.plugin.service.ui.MaxSpeedDisplaySender;
import com.eaglesakura.andriders.plugin.service.ui.SpeedDisplaySender;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.display.DisplayLayoutController;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Initializer;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.garnet.Singleton;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.support.SupportCancelCallbackBuilder;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * アプリのデータ移行補助を行う
 */
@Singleton
public class DataMigrationManager {
    @NonNull
    final Context mContext;

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    @Inject(AppManagerProvider.class)
    PluginDataManager mPluginDataManager;

    /**
     * ACE v3初期リリース
     */
    static final int RELEASE_ACE3 = 1;

    public DataMigrationManager(Context context) {
        mContext = context;
    }

    @Initializer
    public void initialize() {
        Garnet.create(this)
                .depend(Context.class, mContext)
                .inject();
    }

    /**
     * ACEv3初期化を行う
     */
    void initializeAceVer3(CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        // 初期プラグインを有効化する
        CentralPluginCollection plugins = mPluginDataManager.listPlugins(PluginDataManager.PluginListingMode.All, cancelCallback);
        plugins.connect(new CentralPlugin.ConnectOption(), cancelCallback);
        plugins.safeEach(plugin -> {
            if (!plugin.getId().startsWith(mContext.getPackageName())) {
                return;
            }

            // プラグインを有効化する
            mPluginDataManager.setActive(plugin, true);
        });
        plugins.disconnect();

        // ディスプレイを配置する
        DisplayLayoutController displayController = new DisplayLayoutController(mContext);

        displayController.load(cancelCallback);
        DisplayLayoutGroup layoutGroup = displayController.getLayoutGroup(null);
        final String PLUGIN_ID = new PluginInformation(mContext, CentralInterfacePluginService.PLUGIN_ID).getId();
        // 左上に速度
        {
            DisplayLayout layout = new DisplayLayout.Builder(DisplayLayout.getSlotId(0, 0))
                    .bind(PLUGIN_ID, new DisplayKey(mContext, SpeedDisplaySender.DISPLAY_ID).getId())
                    .build();
            layoutGroup.insertOrReplace(layout);
        }
        // 右上に最高速度
        {
            DisplayLayout layout = new DisplayLayout.Builder(DisplayLayout.getSlotId(1, 0))
                    .bind(PLUGIN_ID, new DisplayKey(mContext, MaxSpeedDisplaySender.DISPLAY_ID).getId())
                    .build();
            layoutGroup.insertOrReplace(layout);
        }
        // 更新
        displayController.commit();
    }

    /**
     * マイグレーションが必要な状態である
     */
    public boolean requireMigration() {
        return mAppSettings.getUpdateCheckProps().getInitializeReleased() != RELEASE_ACE3;
    }

    /**
     * データマイグレーションを実行する。
     * マイグレーションは安全のためキャンセルは不可能。
     */
    public void migration() throws AppException {
        try {
            CancelCallback cancelCallback =
                    SupportCancelCallbackBuilder.from(() -> false).orTimeout(1000 * 60 * 30, TimeUnit.MILLISECONDS).build();
            int release = mAppSettings.getUpdateCheckProps().getInitializeReleased();
            if (release < RELEASE_ACE3) {
                initializeAceVer3(cancelCallback);
                release = RELEASE_ACE3;
            }

            // 無事に初期化完了
            mAppSettings.getUpdateCheckProps().setInitializeReleased(release);
            mAppSettings.commit();
        } catch (Throwable e) {
            AppException.throwAppException(e);
        }
    }
}
