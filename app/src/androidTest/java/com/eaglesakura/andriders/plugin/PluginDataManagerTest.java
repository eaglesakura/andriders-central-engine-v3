package com.eaglesakura.andriders.plugin;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;

import org.junit.Test;

public class PluginDataManagerTest extends AppDeviceTestCase {

    @Inject(AppManagerProvider.class)
    PluginDataManager mDataManager;

    @Override
    public void onSetup() {
        super.onSetup();
        Garnet.inject(this);
    }

    @Test
    public void 必要なServiceのパッケージを列挙できる() throws Throwable {
        validate(mDataManager.listExtensionServices(false))
                .notEmpty().allNotNull()
                .each(info -> {
                    assertNotNull(info.serviceInfo);
                    assertNotEmpty(info.serviceInfo.packageName);
                    assertNotEmpty(info.serviceInfo.name);

                    AppLog.test("Plugin Service[%s@%s]", info.serviceInfo.packageName, info.serviceInfo.name);
                });
    }

    @Test
    public void Pluginクラスを生成できる() throws Throwable {
        CentralPluginCollection plugins = mDataManager.listPlugins(PluginDataManager.PluginListingMode.All, () -> false);
        assertNotNull(plugins);
        validate(plugins.list())
                .allNotNull()
                .sizeIs(mDataManager.listExtensionServices(false).size())
                .each(plugin -> {
                    // 生成直後は未接続である
                    assertFalse(plugin.isConnected());
                    assertNotNull(plugin.getPackageInfo());
                    assertNotNull(plugin.getComponentName());
                    assertNotEmpty(plugin.getName());
                });
    }

    @Test
    public void 全てのPluginに接続切断完了する() throws Throwable {
        CentralPluginCollection plugins = mDataManager.listPlugins(PluginDataManager.PluginListingMode.All, () -> false);

        // 接続テスト
        CentralPlugin.ConnectOption option = new CentralPlugin.ConnectOption();
        plugins.connect(option, () -> false);
        plugins.each(plugin -> {
            assertTrue(plugin.isConnected());
            AppLog.test("Component[%s] sdk[%s]", plugin.getComponentName().toString(), plugin.getSdkVersion());
            assertNotEmpty(plugin.getSdkVersion());
            validate(plugin.getInformation()).notNull()
                    .check(info -> {
                        assertNotEmpty(info.getId());
                    });
            assertNotNull(plugin.loadIcon());
            validate(plugin.getDisplayInformationList()).allNotNull();
        });

        plugins.disconnect();
        plugins.each(plugin -> {
            assertFalse(plugin.isConnected());
        });
    }

    @Test
    public void 全てのPluginをアクティブに出来る() throws Throwable {
        CentralPluginCollection plugins = mDataManager.listPlugins(PluginDataManager.PluginListingMode.All, () -> false);

        CentralPlugin.ConnectOption option = new CentralPlugin.ConnectOption();
        plugins.connect(option, () -> false);
        plugins.each(plugin -> {
            assertFalse(mDataManager.isActive(plugin));

            mDataManager.setActive(plugin, true);

            assertTrue(mDataManager.isActive(plugin));
        });

        plugins.disconnect();
    }
}