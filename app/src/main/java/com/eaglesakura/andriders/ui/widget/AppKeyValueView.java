package com.eaglesakura.andriders.ui.widget;

import com.eaglesakura.andriders.R;
import com.eaglesakura.android.aquery.AQuery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.databinding.BindingMethod;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Key-Valueの表記を行うView
 */
public class AppKeyValueView extends LinearLayout {
    TextView mKeyText;

    TextView mValueText;

    public AppKeyValueView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public AppKeyValueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AppKeyValueView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @SuppressLint("all")
    void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[]{
                R.attr.keyText,
                R.attr.valueText
        });
        {
            mKeyText = new AppCompatTextView(context, attrs);

            LinearLayout.LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            addView(mKeyText, params);
        }
        {
            mValueText = new AppCompatTextView(context, attrs);

            LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addView(mValueText, params);
        }

        setKeyText(typedArray.getString(0));
        setValueText(typedArray.getString(1));
    }

    public void setKeyText(CharSequence text) {
        if (text == null) {
            text = "";
        }
        mKeyText.setText(text);
    }

    public void setValueText(CharSequence text) {
        if (text == null) {
            text = "";
        }
        mValueText.setText(text);
    }

    @BindingAdapter({"keyText"})
    public static void setKeyText(AppKeyValueView view, CharSequence text) {
        view.setKeyText(text);
    }

    @BindingAdapter({"valueText"})
    public static void setValueText(AppKeyValueView view, CharSequence text) {
        view.setValueText(text);
    }
}
