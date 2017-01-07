package com.eaglesakura.andriders.data.common;

import com.eaglesakura.andriders.system.context.config.FbLink;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * リンク情報
 */
public class Link {
    @NonNull
    FbLink mRaw;

    public Link(FbLink raw) {
        mRaw = raw;
    }

    @NonNull
    public String getTitle() {
        return mRaw.title;
    }

    @Nullable
    public String getIconUrl() {
        return mRaw.iconUrl;
    }

    @Nullable
    public Uri getIconUri() {
        return Uri.parse(getIconUrl());
    }

    @NonNull
    public String getLinkUrl() {
        return mRaw.linkUrl;
    }

    public Uri getLinkUri() {
        return Uri.parse(getLinkUrl());
    }
}
