package com.eaglesakura.andriders.central.session;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.gen.prop.CentralServiceSettings;
import com.eaglesakura.andriders.gen.prop.UserProfiles;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.property.ImmutableTextPropertyStore;
import com.eaglesakura.android.property.TextPropertyStore;
import com.eaglesakura.android.property.model.PropertySource;
import com.eaglesakura.json.JSON;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Map;

/**
 * セッション管理情報
 */
public class SessionInfo {

    /**
     * リソースContext
     */
    Context mContext;

    /**
     * セッションを一意に管理するID
     */
    long mSessionId;


    /**
     * セッションを管理する大本の時計
     */
    Clock mSessionClock;

    /**
     * ユーザーのプロファイル管理
     */
    UserProfiles mUserProfiles;

    /**
     * Central状態管理
     */
    CentralServiceSettings mCentralServiceSettings;

    /**
     * デバッグ状態であればtrue
     */
    boolean mDebugable;

    protected SessionInfo(Builder builder) {
        mContext = builder.mContext;
        mSessionId = builder.mSessionId;
        mSessionClock = builder.mSessionClock;
        mUserProfiles = builder.mUserProfiles;
        mCentralServiceSettings = builder.mCentralServiceSettings;
        mDebugable = builder.mDebugable;
    }

    public Context getContext() {
        return mContext;
    }

    public long getSessionId() {
        return mSessionId;
    }

    public Clock getSessionClock() {
        return mSessionClock;
    }

    public UserProfiles getUserProfiles() {
        return mUserProfiles;
    }

    public CentralServiceSettings getCentralServiceSettings() {
        return mCentralServiceSettings;
    }

    public boolean isDebugable() {
        return mDebugable;
    }

    /**
     * ダンプ用のPropertyを取得する
     */
    public TextPropertyStore getProperties() {
        return (TextPropertyStore) mCentralServiceSettings.getPropertyStore();
    }

    public static class Builder {

        /**
         * リソースContext
         */
        Context mContext;

        /**
         * セッションを一意に管理するID
         */
        long mSessionId;


        /**
         * セッションを管理する大本の時計
         */
        Clock mSessionClock;

        /**
         * ユーザーのプロファイル管理
         */
        UserProfiles mUserProfiles;

        /**
         * Central状態管理
         */
        CentralServiceSettings mCentralServiceSettings;

        /**
         * デバッグチェック
         */
        boolean mDebugable;

        public Builder(@NonNull Context context, @NonNull Clock clock) {
            mContext = context;
            mSessionClock = clock;
            mSessionId = clock.now();
        }

        public Builder profile(String profileMapJson) throws IOException {
            return profile(JSON.decode(profileMapJson, Map.class));
        }

        public Builder profile(Map<String, String> profileMap) throws IOException {
            PropertySource propertySource = AppSupportUtil.loadPropertySource(mContext, R.raw.central_properties);
            ImmutableTextPropertyStore kvs = new ImmutableTextPropertyStore(propertySource, profileMap);

            mUserProfiles = new UserProfiles(kvs);
            mCentralServiceSettings = new CentralServiceSettings(kvs);
            return this;
        }

        public Builder debugable(boolean debug) {
            mDebugable = debug;
            return this;
        }


        public SessionInfo build() {
            return new SessionInfo(this);
        }
    }
}
