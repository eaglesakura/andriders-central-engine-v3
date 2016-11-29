package com.eaglesakura.andriders.data.display;

import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.display.DisplayData;
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
 *
 * MEMO: Clockの時刻は外部要因によって進める必要があり、内部では操作を行わない。
 */
public class DisplayBindManager {

    @NonNull
    final Context mContext;

    @NonNull
    final Clock mClock;

    final Object lock = new Object();

    /**
     * 規定の表示内容一覧
     */
    Map<String, ValueHolder> mValues = new HashMap<>();

    public DisplayBindManager(@NonNull Context context, @NonNull Clock clock) {
        mContext = context.getApplicationContext();
        mClock = clock;
    }

    /**
     * 拡張サービスから受信した表示データを保存する
     *
     * @param plugin 受信したプラグイン
     * @param values 表示内容
     */
    public void putValue(final CentralPlugin plugin, List<DisplayData> values) {
        synchronized (lock) {
            for (DisplayData value : values) {
                String key = createKey(plugin, value);
                ValueHolder holder = mValues.get(key);
                if (holder == null) {
                    holder = new ValueHolder(value);
                    mValues.put(key, holder);
                } else {
                    holder.update(value);
                }
            }
        }
    }


    /**
     * スロット表示内容を更新させる
     */
    @UiThread
    public int bind(@NonNull CentralPlugin plugin, @NonNull DisplayKey info, @NonNull DataViewBinder binder, @NonNull ViewGroup slotRoot) {
        AndroidThreadUtil.assertUIThread();
        synchronized (lock) {
            ValueHolder holder = mValues.get(createKey(plugin, info));
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
     * @param plugin 拡張サービス名
     * @param info   表示データ名
     */
    @Nullable
    public DisplayData getValue(CentralPlugin plugin, DisplayKey info) {
        synchronized (lock) {
            ValueHolder holder = mValues.get(createKey(plugin, info));
            if (holder != null) {
                return holder.mData;
            } else {
                return null;
            }
        }
    }

    private class ValueHolder {
        /**
         * 更新時刻
         */
        long mDate;

        /**
         * 受信データ
         */
        DisplayData mData;

        ValueHolder(DisplayData data) {
            update(data);
        }

        void update(DisplayData data) {
            mDate = mClock.now();
            mData = data;
        }
    }

    /**
     * 管理用のキーに変換する
     */
    private String createKey(CentralPlugin plugin, DisplayData displayValue) {
        return plugin.getId() + "@" + displayValue.getId();
    }

    /**
     * 管理用のキーに変換する
     */
    private String createKey(CentralPlugin plugin, DisplayKey info) {
        return plugin.getId() + "@" + info.getId();
    }

    public interface Holder {
        DisplayBindManager getDisplayBindManager();
    }
}
