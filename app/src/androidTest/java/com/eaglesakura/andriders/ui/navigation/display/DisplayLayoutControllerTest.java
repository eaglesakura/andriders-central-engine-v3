package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.model.display.DisplayLayoutGroup;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.CentralPluginCollection;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.android.garnet.Garnet;

import org.junit.Test;

public class DisplayLayoutControllerTest extends AppDeviceTestCase {

    @Override
    public void onSetup() {
        super.onSetup();

        // デフォルトプラグインを初期化する
        try {
            PluginDataManager pluginManager = Garnet.instance(AppManagerProvider.class, PluginDataManager.class);
            CentralPluginCollection collection = pluginManager.listPlugins(PluginDataManager.PluginListingMode.All, () -> false);
            collection.connect(new CentralPlugin.ConnectOption(), () -> false);
            for (CentralPlugin plugin : collection.listDisplayPlugins()) {
                pluginManager.setActive(plugin, true);
            }
            collection.disconnect();
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void 初期ロードが行える() throws Throwable {
        DisplayLayoutController controller = new DisplayLayoutController(getContext());
        controller.load(() -> false);

        validate(controller.mDisplayPlugins.list()).notEmpty().allNotNull().each(plugin -> {
            assertNotNull(plugin.loadIcon());
            assertNotEmpty(plugin.getName());
            assertNotNull(plugin.getCategory());
            validate(plugin.listDisplayKeys().list()).notEmpty().allNotNull();
        });
    }

    @Test
    public void データの保存が行える() throws Throwable {
        {
            DisplayLayoutController controller = new DisplayLayoutController(getContext());
            controller.load(() -> false);

            DisplayLayoutGroup layoutGroup = controller.getLayoutGroup(null);
            assertNotNull(layoutGroup);

            controller.commit();
        }

        {
            DisplayLayoutController controller = new DisplayLayoutController(getContext());
            controller.load(() -> false);
            validate(controller.mLayouts.size()).from(1);
        }
    }

    @Test
    public void アプリ構成の削除が行える() throws Throwable {

        // 初回保存
        {
            DisplayLayoutController controller = new DisplayLayoutController(getContext());
            controller.load(() -> false);
            DisplayLayoutGroup layoutGroup = controller.getLayoutGroup("com.example");
            assertNotNull(layoutGroup);

            controller.commit();
        }
        {
            DisplayLayoutController controller = new DisplayLayoutController(getContext());
            controller.load(() -> false);
            assertNotNull(controller.mLayouts.get("com.example"));
            controller.remove("com.example");

            assertNull(controller.mLayouts.get("com.example"));
        }
        {
            DisplayLayoutController controller = new DisplayLayoutController(getContext());
            controller.load(() -> false);
            assertNull(controller.mLayouts.get("com.example"));
        }
    }
}