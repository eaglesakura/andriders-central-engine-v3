package com.eaglesakura.andriders.data.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;
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
     * @param values 表示内容
     */
    public void putValue(List<DisplayData> values) {
        synchronized (lock) {
            for (DisplayData value : values) {
                String key = value.getId();
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

    private DataViewBinder getBinder(View rootView, int slotId) {
        ViewGroup stub = (ViewGroup) rootView.findViewById(slotId);
        DataViewBinder binder = (DataViewBinder) stub.getTag(R.id.Tag_SlotViewBinder);
        if (binder == null) {
            binder = new DataViewBinder(mContext, stub, mClock);
            stub.setTag(R.id.Tag_SlotViewBinder, binder);
        }
        return binder;
    }

    /**
     * スロット表示内容を更新させる
     */
    @UiThread
    public void bind(@NonNull DisplayLayout layout, @NonNull ViewGroup rootView) {
        AndroidThreadUtil.assertUIThread();
        synchronized (lock) {
            DataViewBinder binder = getBinder(rootView, layout.getSlotId());

            if (StringUtil.isEmpty(layout.getValueId())) {
                // 値が設定されていないので、このスロットはブランクである
                binder.getSlotRoot().setVisibility(View.INVISIBLE);
            } else {
                binder.getSlotRoot().setVisibility(View.VISIBLE);
                ValueHolder holder = mValues.get(layout.getValueId());

                if (holder == null) {
                    binder.bind(null, 0);
                } else {
                    binder.bind(holder.mData, holder.mDate);
                }
            }
        }
    }


    /**
     * 拡張サービスから表示データを取得する
     *
     * @param info 表示データ名
     */
    @Nullable
    public DisplayData getValue(DisplayKey info) {
        synchronized (lock) {
            ValueHolder holder = mValues.get(info.getId());
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

    public interface Holder {
        DisplayBindManager getDisplayBindManager();
    }
}
