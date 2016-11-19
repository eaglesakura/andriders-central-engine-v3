package com.eaglesakura.andriders.model.display;

import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.util.StringUtil;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 1アプリごとのディスプレイ情報
 */
public class DisplayLayoutGroup extends DataCollection<DisplayLayout> {

    /**
     * 管理対象のアプリPackage名
     */
    final String mPackageName;

    public DisplayLayoutGroup(List<DisplayLayout> dataList, @NonNull String packageName) {
        super(dataList);
        mPackageName = packageName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    /**
     * データの挿入か更新を行う
     */
    public void insertOrReplace(DisplayLayout layout) {
        int index = getSource().indexOf(layout);
        if (index < 0) {
            getSource().add(layout);
        } else {
            getSource().set(index, layout);
        }
    }

    /**
     * 最終更新日を取得する
     *
     * データが0件の場合、nullを返却する
     */
    @Nullable
    public Date getUpdateDate() {
        long latest = 0;
        for (DisplayLayout layout : getSource()) {
            if (latest == 0 || layout.getUpdatedDate().getTime() < latest) {
                latest = layout.getUpdatedDate().getTime();
            }
        }

        if (latest == 0) {
            return null;
        } else {
            return new Date(latest);
        }
    }

    /**
     * ソート順管理
     */
    public static final Comparator<DisplayLayoutGroup> COMPARATOR_ASC = (a, b) -> {
        Date aDate = a.getUpdateDate();
        Date bDate = b.getUpdateDate();

        if (aDate != null && bDate != null) {
            return Long.compare(aDate.getTime(), bDate.getTime());
        } else if (aDate == null && bDate != null) {
            return 1;
        } else if (aDate != null && bDate != null) {
            return -1;
        } else {
            return StringUtil.compareString(a.getPackageName(), b.getPackageName());
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisplayLayoutGroup that = (DisplayLayoutGroup) o;

        return mPackageName != null ? mPackageName.equals(that.mPackageName) : that.mPackageName == null;

    }

    @Override
    public int hashCode() {
        return mPackageName != null ? mPackageName.hashCode() : 0;
    }
}
