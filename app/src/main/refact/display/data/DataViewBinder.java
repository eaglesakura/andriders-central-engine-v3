package com.eaglesakura.andriders.display.data;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.plugin.display.BasicValue;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.plugin.display.LineValue;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * スロットごとに割り当てられたView/Dataをバインディングする
 */
public class DataViewBinder {
    final Clock mClock;

    @NonNull
    final Context mContext;

    public DataViewBinder(@NonNull Context context, @NonNull Clock clock) {
        mClock = clock;
        mContext = context.getApplicationContext();
    }

    public static final int BIND_RESULT_INFLATE = 0x1 << 0;
    public static final int BIND_RESULT_BASICVALUE = 0x1 << 1;
    public static final int BIND_RESULT_LINEVALUE = 0x1 << 2;
    public static final int BIND_RESULT_NAVLUE = 0x1 << 3;

    public int bind(@NonNull ViewGroup slotRoot, @Nullable DisplayData data, long dataTime) {
        AndroidThreadUtil.assertUIThread();

        int result = 0;
        if (slotRoot.getChildCount() == 0) {
            // 子を生成する
            inflate(slotRoot);
            result |= BIND_RESULT_INFLATE;
        }

        AQuery q = new AQuery(slotRoot);
        if (data == null || (data.hasTimeout() && mClock.absDiff(dataTime) > data.getTimeoutMs())) {
            // タイムアウトしている
            resetView(q, VISIBLE_NA_VALUE);
            return (result | BIND_RESULT_NAVLUE);
        }

        if (data.getBasicValue() != null) {
            bind(slotRoot.getId(), q, data.getBasicValue());
            result |= BIND_RESULT_BASICVALUE;
        } else if (data.getLineValue() != null) {
            bind(q, data.getLineValue());
            result |= BIND_RESULT_LINEVALUE;
        } else {
            resetView(q, VISIBLE_NA_VALUE);
            result |= BIND_RESULT_NAVLUE;
        }

        return result;
    }

    /**
     * 行テキストを表示する
     */
    private void bind(AQuery q, LineValue value) {
        resetView(q, VISIBLE_LINE_VALUE);

        LinearLayout root = q.id(R.id.Service_Central_Display_Lines_Root).getView(LinearLayout.class);
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

    /**
     * 標準内容のテキストを表示する
     */
    private void bind(int slotId, AQuery q, BasicValue value) {
        resetView(q, VISIBLE_BASIC_VALUE);

        updateOrGone(q.id(R.id.Service_Central_Display_Basic_Value).getTextView(), value.getValue());
        updateOrGone(q.id(R.id.Service_Central_Display_Basic_Title).getTextView(), value.getTitle());
        if (LayoutSlot.isLeft(slotId)) {
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
     * 表示用のViewを構築する
     */
    private void inflate(ViewGroup slotRoot) {
        // 表示用のViewをInflate
        View root = View.inflate(mContext, R.layout.central_display_slot, null);

        ViewGroup lineViewRoot = (ViewGroup) root.findViewById(R.id.Service_Central_Display_Lines_Root);

        // 子を必要に応じて登録する
        while (lineViewRoot.getChildCount() < LineValue.MAX_LINES) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;

            View view = View.inflate(mContext, R.layout.central_display_keyvalue_row, null);
            view.setVisibility(View.GONE);
            lineViewRoot.addView(view, params);
        }

        slotRoot.addView(root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
            View layout = View.inflate(root.getContext(), R.layout.central_display_slot, null);
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
