package com.eaglesakura.andriders.system.context.config;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * リンク情報を含んだConfig値
 */
@Keep
public class FbLink {
    @NonNull
    public String title;

    @NonNull
    public String linkUrl;

    @Nullable
    public String iconUrl;
}
