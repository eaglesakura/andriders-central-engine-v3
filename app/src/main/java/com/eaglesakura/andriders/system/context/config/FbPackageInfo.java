package com.eaglesakura.andriders.system.context.config;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * パッケージ情報
 */
@Keep
public class FbPackageInfo {

    @NonNull
    public String title;

    @NonNull
    public String packageName;

    @Nullable
    public String className;
}
