package com.eaglesakura.andriders.model.display;

import com.eaglesakura.andriders.dao.central.DbDisplayLayout;
import com.eaglesakura.andriders.data.display.DisplayLayoutManager;
import com.eaglesakura.andriders.model.DaoModel;
import com.eaglesakura.util.StringUtil;

import org.greenrobot.greendao.annotation.NotNull;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * ディスプレイ表示対象のアイテム
 */
public class DisplayLayout extends DaoModel<DbDisplayLayout> {

    /**
     * デフォルトパッケージ名
     * 現実的に存在し得ない。
     */
    public static final String PACKAGE_NAME_DEFAULT = ".null";

    public DisplayLayout(@NonNull DbDisplayLayout raw) {
        super(raw);
    }

    @NotNull
    public String getUniqueId() {
        return mRaw.getUniqueId();
    }

    @NotNull
    public Date getUpdatedDate() {
        return mRaw.getUpdatedDate();
    }

    @NotNull
    public String getAppPackageName() {
        return mRaw.getAppPackageName();
    }

    /**
     * スロット位置を一意に識別可能なIDを取得する
     */
    public int getSlotId() {
        return mRaw.getSlotId();
    }

    /**
     * このスロットを表示させるためのPluginIDを取得する
     */
    @NotNull
    public String getPluginId() {
        return mRaw.getPluginId();
    }

    /**
     * このスロットに表示させるべき値を取得する
     */
    @NotNull
    public String getValueId() {
        return mRaw.getValueId();
    }

    /**
     * アイテムのX位置を取得する
     */
    public int getSlotX() {
        return getSlotX(getSlotId());
    }

    /**
     * アイテムのY位置を取得する
     */
    public int getSlotY() {
        return getSlotY(getSlotId());
    }

    /**
     * 左側のスロットである場合true
     */
    public boolean isLeft() {
        return getSlotX() == 0;
    }

    /**
     * 右側のスロットである場合true
     */
    public boolean isRight() {
        return getSlotX() == (DisplayLayoutManager.MAX_HORIZONTAL_SLOTS - 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return ((DisplayLayout) obj).getUniqueId().equals(getUniqueId());
    }

    @Override
    public int hashCode() {
        return getUniqueId().hashCode();
    }

    /**
     * X位置を取得する
     *
     * @param id スロットIDEALLY
     */
    @IntRange(from = 0)
    public static int getSlotX(int id) {
        return id & 0xFF;
    }

    /**
     * Y位置を取得する
     *
     * @param id スロットID
     * @return Y位置
     */
    @IntRange(from = 0)
    public static int getSlotY(int id) {
        return (id >> 8) & 0xFF;
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

    /**
     * 値の生成を行わせる
     */
    public static class Builder {
        DbDisplayLayout mRaw = new DbDisplayLayout();

        public Builder(int slotId) {
            mRaw.setSlotId(slotId);
            mRaw.setAppPackageName(PACKAGE_NAME_DEFAULT);
        }

        public Builder(DisplayLayout origin) {
            mRaw.setSlotId(origin.getSlotId());
            mRaw.setAppPackageName(origin.getAppPackageName());
        }

        /**
         * 表示対象のアプリ
         *
         * @param packageName アプリPackage名
         */
        public Builder application(String packageName) {
            mRaw.setAppPackageName(packageName);
            return this;
        }

        /**
         * 表示対象のプラグイン
         *
         * @param pluginId 表示担当プラグイン
         * @param valueId  表示値の管理ID
         */
        public Builder bind(String pluginId, String valueId) {
            mRaw.setPluginId(pluginId);
            mRaw.setValueId(valueId);
            return this;
        }

        public DisplayLayout build() {
            mRaw.setUniqueId(StringUtil.format("%s@%02d%02d", mRaw.getAppPackageName(),
                    getSlotY(mRaw.getSlotId()),
                    getSlotX(mRaw.getSlotId())
            ));
            mRaw.setUpdatedDate(new Date());
            return new DisplayLayout(mRaw);
        }
    }
}
