package com.eaglesakura.andriders.display;

import com.eaglesakura.andriders.computer.extension.client.ExtensionClient;
import com.eaglesakura.andriders.extension.DisplayInformation;
import com.eaglesakura.andriders.extension.display.DisplayData;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.util.AndroidThreadUtil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * サイコンのディスプレイ管理を行う
 */
public class DataDisplayManager {

    @NonNull
    final Context mContext;

    @NonNull
    final Clock mClock;

    final Object lock = new Object();

    /**
     * 規定の表示内容一覧
     */
    Map<String, ValueHolder> mValues = new HashMap<>();

    public DataDisplayManager(@NonNull Context context, @NonNull Clock clock) {
        mContext = context.getApplicationContext();
        mClock = clock;
    }

    /**
     * 拡張サービスから受信した表示データを保存する
     *
     * @param extension 受信した拡張サービス名
     * @param values    表示内容
     */
    public void putValue(final ExtensionClient extension, List<DisplayData> values) {
        synchronized (lock) {
            for (DisplayData value : values) {
                mValues.put(createKey(extension, value), new ValueHolder(value));
            }
        }
    }

    /**
     * スロット表示内容を更新させる
     */
    @UiThread
    public int bind(@NonNull ExtensionClient extension, @NonNull DisplayInformation info, @NonNull DataViewBinder binder, @NonNull ViewGroup slotRoot) {
        AndroidThreadUtil.assertUIThread();
        synchronized (lock) {
            ValueHolder holder = mValues.get(createKey(extension, info));
            if (holder == null) {
                binder.bind(slotRoot, null, 0);
            } else {
                binder.bind(slotRoot, holder.mData, holder.mDate);
            }
            return 0;
        }
    }

    /**
     * 拡張サービスから表示データを取得する
     *
     * @param extension 拡張サービス名
     * @param info      表示データ名
     */
    @Nullable
    public DisplayData getValue(ExtensionClient extension, DisplayInformation info) {
        synchronized (lock) {
            ValueHolder holder = mValues.get(createKey(extension, info));
            if (holder != null) {
                return holder.mData;
            } else {
                return null;
            }
        }
    }

    class ValueHolder {
        /**
         * 更新時刻
         */
        final long mDate = mClock.now();

        /**
         * 受信データ
         */
        final DisplayData mData;

        ValueHolder(DisplayData data) {
            mData = data;
        }
    }

    /**
     * 管理用のキーに変換する
     */
    private String createKey(ExtensionClient extension, DisplayData displayValue) {
        return String.format("%s@%s", extension.getInformation().getId(), displayValue.getId());
    }

    /**
     * 管理用のキーに変換する
     */
    private String createKey(ExtensionClient extension, DisplayInformation info) {
        return String.format("%s@%s", extension.getInformation().getId(), info.getId());
    }
}
