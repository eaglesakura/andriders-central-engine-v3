package com.eaglesakura.andriders.system.context.config;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;

/**
 * Firebaseのコンフィグ情報Root
 */
@Keep
public class FbConfigRoot {
    @NonNull
    public FbProfile profile;

    @NonNull
    public FbSenor sensor;

    @NonNull
    public FbAboutInfo aboutInfo;
}
