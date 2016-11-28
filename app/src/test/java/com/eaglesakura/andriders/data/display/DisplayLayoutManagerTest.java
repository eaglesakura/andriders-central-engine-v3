package com.eaglesakura.andriders.data.display;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.model.display.DisplayLayoutCollection;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.android.garnet.Garnet;

import org.junit.Test;

public class DisplayLayoutManagerTest extends AppUnitTestCase {

    @Test
    public void 削除が行える() throws Throwable {
        DisplayLayoutManager layoutManager = Garnet.instance(AppManagerProvider.class, DisplayLayoutManager.class);
        assertNotNull(layoutManager);
        DisplayLayout origin;
        origin = new DisplayLayout.Builder(DisplayLayout.getSlotId(0, 3))
                .bind("pluginId", "dataId")
                .application("com.example")
                .build();
        // データの挿入が行える
        layoutManager.update(origin);
        validate(layoutManager.list().list()).sizeIs(1);
        layoutManager.remove(origin);
        validate(layoutManager.list().list()).sizeIs(0);
    }

    @Test
    public void 全件削除が行える() throws Throwable {
        DisplayLayoutManager layoutManager = Garnet.instance(AppManagerProvider.class, DisplayLayoutManager.class);
        assertNotNull(layoutManager);
        DisplayLayout origin;
        origin = new DisplayLayout.Builder(DisplayLayout.getSlotId(0, 3))
                .bind("pluginId", "dataId")
                .application("com.example")
                .build();
        // データの挿入が行える
        layoutManager.update(origin);
        validate(layoutManager.list().list()).sizeIs(1);
        layoutManager.removeAll("com.example");
        validate(layoutManager.list().list()).sizeIs(0);
    }

    @Test
    public void 保存と読み込みが行える() throws Throwable {
        DisplayLayoutManager layoutManager = Garnet.instance(AppManagerProvider.class, DisplayLayoutManager.class);
        assertNotNull(layoutManager);

        // 初期は0件のロードが行える
        {
            DisplayLayoutCollection list = layoutManager.list();
            assertNotNull(list);
            validate(list.list()).sizeIs(0);
        }
        // データをデフォルトで挿入する
        DisplayLayout origin;
        DisplayLayout loaded;
        {
            origin = new DisplayLayout.Builder(DisplayLayout.getSlotId(0, 3))
                    .bind("pluginId", "dataId")
                    .application("com.example")
                    .build();
            assertNotNull(origin);
            assertNotEmpty(origin.getUniqueId());
            assertNotEmpty(origin.getAppPackageName());
            assertNotEmpty(origin.getPluginId());
            assertNotEmpty(origin.getValueId());
            assertNotNull(origin.getUpdatedDate());

            // データの挿入が行える
            layoutManager.update(origin);
        }

        // 指定したオブジェクトがロードできる
        {
            DisplayLayoutCollection list = layoutManager.list();
            validate(list.list()).sizeIs(1);
            loaded = list.list().get(0);
            assertEquals(loaded, origin);
        }

        // 別packageは引っかからない
        {
            DisplayLayoutCollection list = layoutManager.list(null);
            validate(list.list()).sizeIs(0);
        }
    }

}