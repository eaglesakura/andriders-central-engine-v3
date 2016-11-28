package com.eaglesakura.andriders.util;

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
}
