package com.eaglesakura.andriders.computer.display.computer;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.display.DisplaySlot;
import com.eaglesakura.andriders.extension.display.BasicValue;
import com.eaglesakura.andriders.extension.display.DisplayData;
import com.eaglesakura.andriders.extension.display.LineValue;
import com.eaglesakura.andriders.protocol.internal.InternalData;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * アプリ内で使用するための拡張設定
 */
public class DisplayViewData extends DisplayData {
    final long createdDate = System.currentTimeMillis();

    public DisplayViewData(InternalData.IdlCycleDisplayValue.Builder raw) {
        super(raw);
    }

    /**
     * 標準内容のテキストを表示する
     */
    private void bind(Context context, View stub, BasicValue value) {
        AQuery q = new AQuery(stub);
        resetView(q, VISIBLE_BASIC_VALUE);

        updateOrGone(q.id(R.id.Service_Central_Display_Basic_Value).getTextView(), value.getValue());
        updateOrGone(q.id(R.id.Service_Central_Display_Basic_Title).getTextView(), value.getTitle());
        if (DisplaySlot.isLeft(stub.getId())) {
            updateOrGone(q.id(R.id.Service_Central_Display_Basic_ZoneTitle_Left).visible().getTextView(), value.getZoneText());
            q.id(R.id.Service_Central_Display_Basic_ZoneTitle_Right).gone();

            // ゾーンカラーを設定する
            if (value.hasZoneBar()) {
                q.id(R.id.Service_Central_Display_Basic_ZoneColor_Left).visible().backgroundColor(value.getBarColorARGB());
                q.id(R.id.Service_Central_Display_Basic_ZoneColor_Right).gone();
            } else {
                q.id(R.id.Service_Central_Display_Basic_ZoneColor_Left).gone();
                q.id(R.id.Service_Central_Display_Basic_ZoneColor_Right).gone();
            }
        } else {
            q.id(R.id.Service_Central_Display_Basic_ZoneTitle_Left).gone();
            updateOrGone(q.id(R.id.Service_Central_Display_Basic_ZoneTitle_Right).visible().getTextView(), value.getZoneText());

            // ゾーンカラーを設定する
            if (value.hasZoneBar()) {
                q.id(R.id.Service_Central_Display_Basic_ZoneColor_Left).gone();
                q.id(R.id.Service_Central_Display_Basic_ZoneColor_Right).visible().backgroundColor(value.getBarColorARGB());
            } else {
                q.id(R.id.Service_Central_Display_Basic_ZoneColor_Left).gone();
                q.id(R.id.Service_Central_Display_Basic_ZoneColor_Right).gone();
            }
        }
    }

    private void updateOrGone(TextView view, String value) {
        if (StringUtil.isEmpty(value)) {
            view.setVisibility(View.GONE);
        } else {
            view.setText(value);
            view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 行テキストを表示する
     */
    private void bind(Context context, View stub, LineValue value) {
        AQuery q = new AQuery(stub);
        resetView(q, VISIBLE_LINE_VALUE);

        LinearLayout root = q.id(R.id.Service_Central_Display_Lines_Root).getView(LinearLayout.class);
        // 子を必要に応じて登録する
        while (root.getChildCount() < LineValue.MAX_LINES) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;

            View view = View.inflate(context, R.layout.service_cycle_keyvalue_row, null);
            root.addView(view, params);
        }

        for (int i = 0; i < root.getChildCount(); ++i) {
            View row = root.getChildAt(i);
            if (i < value.getLineNum()) {
                // 値を設定
                row.setVisibility(View.VISIBLE);
                updateOrGone((TextView) row.findViewById(R.id.Service_Central_Display_KeyValue_Title), value.getTitle(i));
                updateOrGone((TextView) row.findViewById(R.id.Service_Central_Display_KeyValue_Value), value.getValue(i));
            } else {
                // 値がないので行を消す
                row.setVisibility(View.GONE);
            }
        }
    }

    public boolean isTimeout() {
        if (getTimeoutMs() <= 0) {
            // 時間が設定しなければタイムアウトしない
            return false;
        } else {
            return (System.currentTimeMillis() - createdDate) > getTimeoutMs();
        }
    }

    public void bindView(@NonNull Context context, @NonNull ViewGroup stub) {
        AndroidThreadUtil.assertUIThread();

        if (isTimeout()) {
            // タイムアウトしたので、N/A扱いにする
            bindNotAvailable(context, stub);
            return;
        }

        if (getBasicValue() != null) {
            bind(context, stub, getBasicValue());
            return;
        } else if (getLineValue() != null) {
            bind(context, stub, getLineValue());
            return;
        } else {
            bindNotAvailable(context, stub);
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
        resetView(q, VISIBLE_NA_VALUE);
    }

    static final int VISIBLE_BASIC_VALUE = 0x1 << 0;
    static final int VISIBLE_LINE_VALUE = 0x1 << 1;
    static final int VISIBLE_NA_VALUE = 0x1 << 2;

    /**
     * 表示内容をリセットさせる
     * 必要であれば、Stubに表示用のViewを入れ込む
     */
    private static void resetView(AQuery q, int visibleFlags) {
        ViewGroup root = q.getView(ViewGroup.class);
        if (root.getChildCount() == 0) {
            //
            // 表示用のViewをInflate
            View layout = View.inflate(root.getContext(), R.layout.service_cycle_slot, null);
            root.addView(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        if ((visibleFlags & VISIBLE_BASIC_VALUE) != 0) {
            q.id(R.id.Service_Central_Display_Basic_Root).visible();
        } else {
            q.id(R.id.Service_Central_Display_Basic_Root).gone();
        }
        if ((visibleFlags & VISIBLE_LINE_VALUE) != 0) {
            q.id(R.id.Service_Central_Display_Lines_Root).visible();
        } else {
            q.id(R.id.Service_Central_Display_Lines_Root).gone();
        }

        if ((visibleFlags & VISIBLE_NA_VALUE) != 0) {
            q.id(R.id.Service_Central_Display_NotConnected).visible();
        } else {
            q.id(R.id.Service_Central_Display_NotConnected).gone();
        }
    }
}
