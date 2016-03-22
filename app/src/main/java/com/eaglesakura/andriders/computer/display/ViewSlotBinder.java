package com.eaglesakura.andriders.computer.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.display.LayoutSlot;
import com.eaglesakura.andriders.extension.display.LineValue;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.android.util.AndroidThreadUtil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * スロットごとに割り当てられたView/Dataをバインディングする
 */
public class ViewSlotBinder {
    final Clock mClock;

    /**
     * 割り当てられたスロット
     */
    final LayoutSlot mSlot;

    /**
     * 表示用の1スロット
     */
    View mSlotViewRoot;

    /**
     * 標準のデータ
     */
    @Bind(R.id.Service_Central_Display_Basic_Root)
    ViewGroup mBasicView;

    /**
     * Key-Valueで複数行表示するデータ
     */
    @Bind(R.id.Service_Central_Display_Lines_Root)
    ViewGroup mLineViewRoot;

    public ViewSlotBinder(@NonNull Clock clock, @NonNull LayoutSlot slot) {
        mClock = clock;
        mSlot = slot;
    }

    /**
     * スロットに表示するためのViewを取得する
     */
    public View getSlotView() {
        return mSlotViewRoot;
    }

    /**
     * 表示用のViewを構築する
     */
    public void inflate(Context context) {
        AndroidThreadUtil.assertUIThread();

        if (mSlotViewRoot != null) {
            throw new IllegalStateException("mSlotViewRoot != null");
        }

        // 表示用のViewをInflate
        mSlotViewRoot = View.inflate(context, R.layout.service_cycle_slot, null);
        MargarineKnife.bind(this, mSlotViewRoot);


        // 子を必要に応じて登録する
        while (mLineViewRoot.getChildCount() < LineValue.MAX_LINES) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;

            View view = View.inflate(context, R.layout.service_cycle_keyvalue_row, null);
            view.setVisibility(View.GONE);
            mLineViewRoot.addView(view, params);
        }
    }

}
