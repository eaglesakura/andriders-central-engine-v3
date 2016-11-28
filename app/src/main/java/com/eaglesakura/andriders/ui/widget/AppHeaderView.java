package com.eaglesakura.andriders.ui.widget;

import com.eaglesakura.andriders.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class AppHeaderView extends FrameLayout {
    AppCompatImageView mIcon;

    AppCompatTextView mTitle;

    public AppHeaderView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public AppHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AppHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }


    void init(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.widget_item_header, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[]{
                R.attr.headerIcon,
                R.attr.headerText
        });

        mIcon = (AppCompatImageView) findViewById(R.id.App_HeaderView_Icon);
        mTitle = (AppCompatTextView) findViewById(R.id.App_HeaderView_Title);

        if (isInEditMode()) {
            // ワークアラウンド
            mIcon = (AppCompatImageView) ((ViewGroup) ((ViewGroup) view).getChildAt(0)).getChildAt(0);
            mTitle = (AppCompatTextView) ((ViewGroup) ((ViewGroup) view).getChildAt(0)).getChildAt(1);
        }

        {
            @DrawableRes int resId = typedArray.getResourceId(0, 0);
            mIcon.setImageResource(resId);
        }
        mTitle.setText(typedArray.getString(1));
    }
}
