package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.ui.widget.IconItemAdapter;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import java.util.Comparator;
import java.util.Date;

/**
 * レイアウト用のアプリケーション情報
 */
public class DisplayLayoutApplication implements IconItemAdapter.Item {
    private Context mContext;

    @Nullable
    private final ApplicationInfo mAppInfo;

    Date mUpdatedDate;

    Drawable mIcon;

    public DisplayLayoutApplication(Context context, @Nullable ApplicationInfo packageInfo) {
        mContext = context;
        mAppInfo = packageInfo;
    }

    /**
     * デフォルト構成のアプリである場合true
     */
    public boolean isDefaultApp() {
        return mAppInfo == null;
    }

    public String getPackageName() {
        if (mAppInfo == null) {
            return DisplayLayout.PACKAGE_NAME_DEFAULT;
        } else {
            return mAppInfo.packageName;
        }
    }

    /**
     * アイコンを取得する
     */
    public Drawable getIcon() {
        if (mIcon == null) {
            if (mAppInfo == null) {
                mIcon = mContext.getDrawable(R.mipmap.ic_launcher);
            } else {
                mIcon = mAppInfo.loadIcon(mContext.getPackageManager());
            }
        }
        return mIcon;
    }

    /**
     * 表示名を取得する
     */

    @Override
    public String getTitle() {
        if (mAppInfo == null) {
            return mContext.getString(R.string.Word_Common_Default);
        } else {
            return mAppInfo.loadLabel(mContext.getPackageManager()).toString();
        }
    }

    /**
     * 昇順ソート
     */
    public static final Comparator<DisplayLayoutApplication> COMPARATOR_ASC = (a, b) -> {
        if (a.mAppInfo == null) {
            return -1;
        } else if (b.mAppInfo == null) {
            return 1;
        }

        if (a.mUpdatedDate != null && b.mUpdatedDate != null) {
            // 値が大きい方を優先させる
            return Long.compare(b.mUpdatedDate.getTime(), a.mUpdatedDate.getTime());
        } else if (a.mUpdatedDate != null) {
            return -1;
        } else if (b.mUpdatedDate != null) {
            return 1;
        } else {
            return StringUtil.compareString(a.mAppInfo.packageName, b.mAppInfo.packageName);
        }
    };
}
