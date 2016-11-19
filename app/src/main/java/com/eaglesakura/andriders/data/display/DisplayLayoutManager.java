package com.eaglesakura.andriders.data.display;

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
     * 指定したpackageのレイアウトすべてを
     */
    @NonNull
    public DisplayLayoutCollection list(@Nullable String packageName) {
        try (CentralSettingDatabase db = open()) {
            return db.listLayouts(wrapPackageName(packageName));
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

//    /**
//     * 既存のカスタマイズ済みレイアウトリストを列挙する
//     */
//    public DataCollection<DbDisplayTarget> listCustomizedDisplays() {
//        try (DisplayLayoutDatabase db = new DisplayLayoutDatabase(mContext).openReadOnly(DisplayLayoutDatabase.class)) {
//            return new DataCollection<>(db.listTargets())
//                    .setComparator((a, b) -> a.getModifiedDate().compareTo(b.getModifiedDate()));
//        }
//    }
//
//    /**
//     * 指定したpackageのレイアウトを削除する
//     */
//    public void deleteLayout(String appPackage) {
//        try (DisplayLayoutDatabase db = new DisplayLayoutDatabase(mContext).openWritable(DisplayLayoutDatabase.class);) {
//            db.remove(appPackage);
//        }
//    }
//
//    public DisplayLayoutManager load(Mode mode, String appPackage) {
//        if (StringUtil.isEmpty(appPackage)) {
//            appPackage = BuildConfig.APPLICATION_ID;
//        }
//
//        DisplayLayoutDatabase db = new DisplayLayoutDatabase(mContext);
//        try {
//            db.openWritable();
//            if (mode == Mode.ReadOnly) {
//                displayTarget = db.loadTarget(appPackage);
//            } else {
//                displayTarget = db.loadTargetOrCreate(appPackage);
//            }
//            List<DbDisplayLayout> layouts = db.listLayouts(displayTarget);
//
//            for (DbDisplayLayout layout : layouts) {
//                LayoutSlot slot = slots.get(layout.getSlotId());
//                if (slot == null) {
//                    // スロットが欠けたので互換性のためデータを削除する
//                    db.remove(layout);
//                } else {
//                    // スロットに表示内容をバインドする
//                    slot.setValueLink(layout);
//                }
//            }
//        } finally {
//            db.close();
//        }
//        return this;
//    }
//
//    /**
//     * 表示内容を指定する
//     *
//     * @param slot      設定対象のスロット
//     * @param extension 拡張機能
//     * @param display   拡張内容
//     */
//    public void setLayout(LayoutSlot slot, PluginInformation extension, DisplayKey display) {
//        slot.setValueLink(displayTarget, extension, display);
//    }
//
//    /**
//     * 表示内容を削除する
//     */
//    public void removeLayout(LayoutSlot slot) {
//        slot.setValueLink(null);
//    }
//
//    /**
//     * データを上書き保存する
//     */
//    public void commit() {
//        DisplayLayoutDatabase db = new DisplayLayoutDatabase(mContext);
//        try {
//            db.openWritable();
//            for (LayoutSlot slot : listSlots()) {
//                DbDisplayLayout displayLayout = slot.getLink();
//                if (displayLayout == null) {
//                    // 表示内容が削除されたのでDBからも削除する
//                    db.remove(displayTarget, slot.getId());
//                } else {
//                    // 表示内容を更新する
//                    db.update(displayLayout);
//                }
//            }
//        } finally {
//            db.close();
//        }
//    }
//
//    /**
//     * 表示のXY位置からスロットを取得する
//     */
//    public LayoutSlot getSlot(int x, int y) {
//        return slots.get(Integer.valueOf(LayoutSlot.getSlotId(x, y)));
//    }
//
//    /**
//     * スロット一覧を取得する。
//     */
//    public List<LayoutSlot> listSlots() {
//        return new ArrayList<>(slots.values());
//    }

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
