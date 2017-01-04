package com.eaglesakura.andriders.system.context.config;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import java.util.List;

/**
 * アプリ開発情報
 */
@Keep
public class FbAboutInfo {
    /**
     * 開発者情報
     */
    @NonNull
    public DeveloperInfo developer;

    /**
     * 開発者情報
     */
    public static class DeveloperInfo {
        @Size(min = 1)
        public List<FbLink> link;
    }
}
