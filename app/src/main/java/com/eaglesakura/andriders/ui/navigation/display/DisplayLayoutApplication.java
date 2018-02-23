package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.ui.widget.IconItemAdapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

/**
 * レイアウト用のアプリケーション情報
 */
public class DisplayLayoutApplication implements IconItemAdapter.Item {
    private Context mContext;

    @Nullable
    private final ApplicationInfo mAppInfo;

    /**
     * バッジとして表示する補助アイコン
     */
    private final Drawable mBadgeIcon;

    /**
     * アプリアイコン本体
     */
    private Drawable mIcon;

    /**
     * アプリタイトル
     */
    private final String mTitle;

    public DisplayLayoutApplication(Context context, @Nullable ApplicationInfo packageInfo, @Nullable Drawable subIcon) {
        mContext = context;
        mAppInfo = packageInfo;
        mBadgeIcon = subIcon;
        if (mAppInfo != null) {
            mTitle = packageInfo.loadLabel(context.getPackageManager()).toString();
        } else {
            mTitle = context.getString(R.string.Word_Common_Default);
        }
    }

    /**
     * デフォルト構成である場合true.
     * ユーザーが明示的に指定しない場合、このレイアウト構成が利用される.
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

    @Override
    public Drawable getBadgeIcon() {
//        return mUpdatedDate != null ? mBadgeIcon : null;
        return mBadgeIcon;
    }

    /**
     * 表示名を取得する
     */
    @Override
    public String getTitle() {
        return mTitle;
    }
}
