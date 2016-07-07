package com.eaglesakura.andriders.ui.widget;

import com.eaglesakura.andriders.R;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.util.ContextUtil;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class AppProgressView extends FrameLayout {
    public AppProgressView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public AppProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AppProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    void init(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater inflater = ContextUtil.getInflater(context);
        View view = inflater.inflate(R.layout.internal_view_progress, null);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[]{
                R.attr.progressText,
        });

        new AQuery(view)
                .id(R.id.EsWidget_Progress_Text).text(typedArray.getText(0));
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(view, params);
    }
}
