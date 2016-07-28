package com.eaglesakura.andriders.ui.widget;

import com.eaglesakura.andriders.R;
import com.eaglesakura.android.util.ContextUtil;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class AppHeaderView extends FrameLayout {
    ImageView mIcon;

    TextView mTitle;

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
        View view = inflater.inflate(R.layout.view_item_header, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[]{
                R.attr.headerIcon,
                R.attr.headerText
        });

        mIcon = (ImageView) findViewById(R.id.App_HeaderView_Icon);
        mTitle = (TextView) findViewById(R.id.App_HeaderView_Title);

        if (isInEditMode()) {
            // ワークアラウンド
            mIcon = (ImageView) ((ViewGroup) ((ViewGroup) view).getChildAt(0)).getChildAt(0);
            mTitle = (TextView) ((ViewGroup) ((ViewGroup) view).getChildAt(0)).getChildAt(1);
        }

        {
            @DrawableRes int resId = typedArray.getResourceId(0, 0);
            VectorDrawableCompat drawableCompat = VectorDrawableCompat.create(context.getResources(), resId, context.getTheme());
            drawableCompat.setTint(ContextCompat.getColor(context, R.color.App_Icon_Grey));
            mIcon.setImageDrawable(drawableCompat);
        }
        mTitle.setText(typedArray.getString(1));
    }
}
