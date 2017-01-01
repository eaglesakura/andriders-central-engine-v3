package com.eaglesakura.andriders.ui.widget;

import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.material.widget.DialogBuilder;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import java.io.Closeable;

/**
 * アプリ用のDialog生成
 */
public class AppDialogBuilder extends DialogBuilder {
    public AppDialogBuilder(AlertDialog.Builder builder) {
        super(builder);
    }

    public static AppDialogBuilder newError(Context context, Throwable error) {
        AppDialogBuilder builder = new AppDialogBuilder(new AlertDialog.Builder(context));
        builder.mBuilder.setTitle(AppUtil.getErrorTitle(error));
        builder.mBuilder.setMessage(AppUtil.getErrorMessage(error));
        return builder;
    }


}
