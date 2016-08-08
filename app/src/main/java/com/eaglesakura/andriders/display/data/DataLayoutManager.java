package com.eaglesakura.andriders.display.data;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.dao.display.DbDisplayLayout;
import com.eaglesakura.andriders.dao.display.DbDisplayTarget;
import com.eaglesakura.andriders.db.display.DisplayLayoutDatabase;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * サイコン表示窓のスロットを管理する
 */
public class DataLayoutManager {
    /**
     * 横方向の最大スロット数
     */
    static final int MAX_HORIZONTAL_SLOTS = 2;

    /**
     * 縦方向の最大スロット数
     */
    static final int MAX_VERTICAL_SLOTS = 7;

    /**
     * 対応しているスロット一覧
     */
    private final Map<Integer, LayoutSlot> slots = new HashMap<>();

    private final Context context;

    private DbDisplayTarget displayTarget;

    public enum Mode {
        /**
         * 読み込みモードとして使用する
         */
        ReadOnly,

        /**
         * 編集モードとして利用する
         */
        Edit,
    }

    public DataLayoutManager(Context context) {
        this.context = context.getApplicationContext();

        for (int x = 0; x < MAX_HORIZONTAL_SLOTS; ++x) {
            for (int y = 0; y < MAX_VERTICAL_SLOTS; ++y) {
                LayoutSlot slot = new LayoutSlot(x, y);
                slots.put(Integer.valueOf(slot.getId()), slot);
            }
        }
    }

    /**
     * 既存のカスタマイズ済みレイアウトリストを列挙する
     */
    public DataCollection<DbDisplayTarget> listCustomizedDisplays() {
        try (DisplayLayoutDatabase db = new DisplayLayoutDatabase(context).openReadOnly(DisplayLayoutDatabase.class)) {
            return new DataCollection<>(db.listTargets())
                    .setComparator((a, b) -> a.getModifiedDate().compareTo(b.getModifiedDate()));
        }
    }

    public DataLayoutManager load(Mode mode, String appPackage) {
        if (StringUtil.isEmpty(appPackage)) {
            appPackage = BuildConfig.APPLICATION_ID;
        }

        DisplayLayoutDatabase db = new DisplayLayoutDatabase(context);
        try {
            db.openWritable();
            if (mode == Mode.ReadOnly) {
                displayTarget = db.loadTarget(appPackage);
            } else {
                displayTarget = db.loadTargetOrCreate(appPackage);
            }
            List<DbDisplayLayout> layouts = db.listLayouts(displayTarget);

            for (DbDisplayLayout layout : layouts) {
                LayoutSlot slot = slots.get(layout.getSlotId());
                if (slot == null) {
                    // スロットが欠けたので互換性のためデータを削除する
                    db.remove(layout);
                } else {
                    // スロットに表示内容をバインドする
                    slot.setValueLink(layout);
                }
            }
        } finally {
            db.close();
        }
        return this;
    }

    /**
     * 表示内容を指定する
     *
     * @param slot      設定対象のスロット
     * @param extension 拡張機能
     * @param display   拡張内容
     */
    public void setLayout(LayoutSlot slot, PluginInformation extension, DisplayKey display) {
        slot.setValueLink(displayTarget, extension, display);
    }

    /**
     * 表示内容を削除する
     */
    public void removeLayout(LayoutSlot slot) {
        slot.setValueLink(null);
    }

    /**
     * データを上書き保存する
     */
    public void commit() {
        DisplayLayoutDatabase db = new DisplayLayoutDatabase(context);
        try {
            db.openWritable();
            for (LayoutSlot slot : listSlots()) {
                DbDisplayLayout displayLayout = slot.getLink();
                if (displayLayout == null) {
                    // 表示内容が削除されたのでDBからも削除する
                    db.remove(displayTarget, slot.getId());
                } else {
                    // 表示内容を更新する
                    db.update(displayLayout);
                }
            }
        } finally {
            db.close();
        }
    }

    /**
     * 表示のXY位置からスロットを取得する
     */
    public LayoutSlot getSlot(int x, int y) {
        return slots.get(Integer.valueOf(LayoutSlot.getSlotId(x, y)));
    }

    /**
     * スロット一覧を取得する。
     */
    public List<LayoutSlot> listSlots() {
        return new ArrayList<>(slots.values());
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
                stub.setId(LayoutSlot.getSlotId(x, y));
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
