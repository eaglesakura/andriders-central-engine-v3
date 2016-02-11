package com.eaglesakura.andriders.computer.display.computer;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.extension.display.BasicValue;
import com.eaglesakura.andriders.extension.display.DisplayData;
import com.eaglesakura.andriders.extension.display.LineValue;
import com.eaglesakura.andriders.idl.display.IdlCycleDisplayValue;
import com.eaglesakura.android.aquery.AQuery;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * アプリ内で使用するための拡張設定
 */
public class DisplayDataImpl extends DisplayData {
    final long createdDate = System.currentTimeMillis();

    public DisplayDataImpl(IdlCycleDisplayValue raw) {
        super(raw);
    }

    /**
     * 標準内容のテキストを表示する
     */
    private void bind(Context context, View stub, BasicValue value) {
        AQuery q = new AQuery(context);
        resetView(q);
    }

    /**
     * 行テキストを表示する
     */
    private void bind(Context context, View stub, LineValue value) {
        AQuery q = new AQuery(context);
        resetView(q);
    }

    public void bindView(@NonNull Context context, @NonNull ViewGroup stub) {
        if ((System.currentTimeMillis() - createdDate) > getTimeoutMs()) {
            // タイムアウトしたので、N/A扱いにする
            bindNotAvailable(context, stub);
            return;
        }

        if (getBasicValue() != null) {
            bind(context, stub, getBasicValue());
            return;
        }
    }

    /**
     * N/Aとして表示させる
     */
    public static void bindNotAvailable(@NonNull Context context, @NonNull ViewGroup stub) {
        if (stub == null) {
            return;
        }
        AQuery q = new AQuery(stub);
        resetView(q);
    }

    /**
     * 表示内容をリセットさせる
     * 必要であれば、Stubに表示用のViewを入れ込む
     */
    private static void resetView(AQuery q) {
        ViewGroup root = q.getView(ViewGroup.class);
        if (root.getChildCount() == 0) {
            //
            // 表示用のViewをInflate
            View layout = View.inflate(root.getContext(), R.layout.service_cycle_slot, null);
            root.addView(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        q.id(R.id.Service_Central_Display_Basic_Root).gone();
        q.id(R.id.Service_Central_Display_Lines_Root).gone();
        q.id(R.id.Service_Central_Display_NotConnected).visible();
    }
}
