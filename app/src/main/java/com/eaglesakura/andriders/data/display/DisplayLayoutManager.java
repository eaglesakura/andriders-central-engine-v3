package com.eaglesakura.andriders.data.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.data.db.CentralSettingDatabase;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.model.display.DisplayLayoutCollection;
import com.eaglesakura.andriders.system.manager.CentralSettingManager;
import com.eaglesakura.android.garnet.Singleton;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.List;

/**
 * サイコン表示窓のスロットを管理する
 */
@Singleton
public class DisplayLayoutManager extends CentralSettingManager {
    /**
     * 横方向の最大スロット数
     */
    public static final int MAX_HORIZONTAL_SLOTS = 2;

    /**
     * 縦方向の最大スロット数
     */
    public static final int MAX_VERTICAL_SLOTS = 7;

    private final Context mContext;

    public DisplayLayoutManager(Context context) {
        super(context);
        mContext = context.getApplicationContext();
    }

    String wrapPackageName(String packageName) {
        if (StringUtil.isEmpty(packageName)) {
            packageName = DisplayLayout.PACKAGE_NAME_DEFAULT;
        }

        return packageName;
    }

    /**
     * 指定したpackageのレイアウトすべてのレイアウトを列挙する
     */
    @NonNull
    public DisplayLayoutCollection list(@Nullable String packageName) {
        try (CentralSettingDatabase db = open()) {
            return db.listLayouts(wrapPackageName(packageName));
        }
    }

    /**
     * 指定したpackageのレイアウトすべてのレイアウトを列挙する
     *
     * レイアウトが空であれば、デフォルト構成を返す。
     */
    @NonNull
    public DisplayLayoutCollection listOrDefault(@Nullable String packageName) {
        DisplayLayoutCollection items = list(packageName);
        if (items.isEmpty()) {
            return list(null);
        } else {
            return items;
        }
    }

    /**
     * すべてのpackageのレイアウトを取得する
     */
    @NonNull
    public DisplayLayoutCollection list() {
        try (CentralSettingDatabase db = open()) {
            return db.listAllLayouts();
        }
    }

    /**
     * レイアウト情報を更新する
     */
    public void update(@NonNull DisplayLayout layout) {
        try (CentralSettingDatabase db = open()) {
            db.update(layout.getRaw());
        }
    }

    /**
     * バッチ処理を行う
     *
     * @param layouts 保存するレイアウト一覧
     */
    public void update(@NonNull List<DisplayLayout> layouts) {
        try (CentralSettingDatabase db = open()) {
            db.runInTx(() -> {
                for (DisplayLayout it : layouts) {
                    db.update(it.getRaw());
                }
                return 0;
            });
        }
    }

    /**
     * レイアウト情報を削除する
     */
    public void remove(@NonNull DisplayLayout layout) {
        try (CentralSettingDatabase db = open()) {
            db.remove(layout.getRaw());
        }
    }

    /**
     * 指定アプリの表示レイアウトを削除する
     */
    public void removeAll(@NonNull String appPackageName) {
        try (CentralSettingDatabase db = open()) {
            db.removeLayouts(appPackageName);
        }
    }

    /**
     * 表示用の仮組みレイアウトを作成する
     *
     * 2x7の格子状のレイアウトを作成し、ViewIdはSlotIdと同じに設定される。
     */
    public static ViewGroup newStubLayout(Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        for (int y = 0; y < MAX_VERTICAL_SLOTS; ++y) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int x = 0; x < MAX_HORIZONTAL_SLOTS; ++x) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.weight = 1.0f;

                FrameLayout stub = new FrameLayout(context);
                stub.setId(DisplayLayout.getSlotId(x, y));
                stub.setTag(R.id.Tag_SlotId, DisplayLayout.getSlotId(x, y));
//                stub.setBackgroundColor(Color.rgb(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255)));
                row.addView(stub, params);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            root.addView(row, params);
        }

        return root;
    }
}
