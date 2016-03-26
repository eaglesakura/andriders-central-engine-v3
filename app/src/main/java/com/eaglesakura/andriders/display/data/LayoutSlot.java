package com.eaglesakura.andriders.display.data;

import com.eaglesakura.andriders.dao.display.DbDisplayLayout;
import com.eaglesakura.andriders.dao.display.DbDisplayTarget;
import com.eaglesakura.andriders.extension.DisplayInformation;
import com.eaglesakura.andriders.extension.ExtensionInformation;

/**
 * ディスプレイの表示位置
 */
public class LayoutSlot {
    private final int x;
    private final int y;
    private DbDisplayLayout db;

    LayoutSlot(int x, int y) {
        this.x = x;
        this.y = y;
    }

    LayoutSlot(int x, int y, DbDisplayLayout db) {
        this.x = x;
        this.y = y;
        this.db = db;
    }

    DbDisplayLayout getLink() {
        return db;
    }

    /**
     * 表示を行う拡張機能Id
     */
    public String getExtensionId() {
        return db.getExtensionId();
    }

    /**
     * 表示内容Id
     */
    public String getDisplayValueId() {
        return db.getValueId();
    }

    /**
     * 表示内容を関連付ける
     *
     * @param target    表示対象のグループ
     * @param extension 表示を行う拡張機能
     * @param display   表示内容
     */
    void setValueLink(DbDisplayTarget target, ExtensionInformation extension, DisplayInformation display) {
        DbDisplayLayout layout = new DbDisplayLayout(String.format("%s/%d", target.getUniqueId(), getId()));
        layout.setValueId(display.getId());
        layout.setExtensionId(extension.getId());
        layout.setTargetPackage(target.getTargetPackage());
        layout.setSlotId(getId());

        db = layout;
    }

    void setValueLink(DbDisplayLayout db) {
        this.db = db;
    }

    /**
     * 値が設定されていたらtrue
     */
    public boolean hasLink() {
        return db != null;
    }

    /**
     * スロットのX座標を取得する
     */
    public int getX() {
        return x;
    }

    /**
     * 左側のスロットである場合true
     */
    public boolean isLeft() {
        return x == 0;
    }

    /**
     * 右側のスロットである場合true
     */
    public boolean isRight() {
        return x == (DataLayoutManager.MAX_HORIZONTAL_SLOTS - 1);
    }

    /**
     * スロットのY座標を取得する
     */
    public int getY() {
        return y;
    }

    /**
     * 一意に識別可能なIDを取得する
     */
    public int getId() {
        return getSlotId(x, y);
    }

    /**
     * スロットを識別するIDを取得する。
     *
     * 現在のところ16bit値として生成するが、将来的な拡張に備えてintとして返す。
     * そのままViewIdの値として指定できるようにする。
     */
    public static int getSlotId(int x, int y) {
        return (y << 8 | x) & 0x0000FFFF;
    }

    /**
     * 左側のアイテムである場合true
     */
    public static boolean isLeft(int slotId) {
        return (slotId % 2) == 0;
    }

    /**
     * 右側のアイテムである場合true
     */
    public static boolean isRight(int slotId) {
        return !isLeft(slotId);
    }
}
