package com.eaglesakura.andriders.util;

import com.eaglesakura.andriders.ui.widget.AppHeaderView;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Data Binding Method
 */
public class AppBindingUtil {
    @BindingAdapter("srcCompat")
    public static void appSrcCompat(ImageView imageView, Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    @BindingAdapter("headerText")
    public static void appHeaderText(AppHeaderView view, CharSequence text) {
        view.setTitle(text);
    }
}
