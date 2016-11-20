package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.model.display.DisplayLayoutGroup;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.CentralPluginCollection;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.util.Util;

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
        validate(controller.mApplications.list()).notEmpty().allNotNull().each(app -> {
            assertNotEmpty(app.loadLabel());
            assertNotNull(app.loadIcon());
            assertNotNull(app.getPackageName());
        });
        // デフォルト構成のアプリは1つだけである
        validate(controller.mApplications.list(it -> it.isDefaultApp())).sizeIs(1);

        // 常にデフォルトアプリが最初に来ている
        assertTrue(controller.listSortedApplications().get(0).isDefaultApp());
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
    public void アプリ一覧取得の順番制御が行える() throws Throwable {
        DisplayLayoutController controller = new DisplayLayoutController(getContext());
        controller.load(() -> false);
        Util.sleep(100);
        controller.getLayoutGroup("com.google.android.apps.maps");
        Util.sleep(100);
        controller.getLayoutGroup("com.google.android.apps.plus");
        Util.sleep(100);

        validate(controller.listSortedApplications())
                .checkAt(0, app -> assertTrue(app.isDefaultApp()))  // トップはデフォルトアプリ固定
                // 新しい取得順に更新される
                .checkAt(1, app -> assertEquals(app.getPackageName(), "com.google.android.apps.plus"))
                .checkAt(2, app -> assertEquals(app.getPackageName(), "com.google.android.apps.maps"));
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