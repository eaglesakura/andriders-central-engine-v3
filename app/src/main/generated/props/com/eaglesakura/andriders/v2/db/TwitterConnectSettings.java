package com.eaglesakura.andriders.v2.db;

import android.content.Context;

public class TwitterConnectSettings extends com.eaglesakura.android.db.BasePropertiesDatabase {

    public static final String ID_FULLNAME = "TwitterConnectSettings.fullName";
    public static final String ID_ID = "TwitterConnectSettings.id";
    public static final String ID_TOKEN = "TwitterConnectSettings.token";
    public static final String ID_TOKENSECRET = "TwitterConnectSettings.tokenSecret";
    public static final String ID_SMALLICONURL = "TwitterConnectSettings.smallIconUrl";
    public static final String ID_LARGEICONURL = "TwitterConnectSettings.largeIconUrl";
    public static final String ID_ICON = "TwitterConnectSettings.icon";

    public TwitterConnectSettings(Context context) {
        super(context, "props.db");
        _initialize();
    }

    public TwitterConnectSettings(Context context, String dbFileName) {
        super(context, dbFileName);
        _initialize();
    }

    protected void _initialize() {

        addProperty("TwitterConnectSettings.fullName", "");
        addProperty("TwitterConnectSettings.id", "");
        addProperty("TwitterConnectSettings.token", "");
        addProperty("TwitterConnectSettings.tokenSecret", "");
        addProperty("TwitterConnectSettings.smallIconUrl", "");
        addProperty("TwitterConnectSettings.largeIconUrl", "");
        addProperty("TwitterConnectSettings.icon", "");

        load();

    }

    public void setFullName(String set) {
        setProperty("TwitterConnectSettings.fullName", set);
    }

    public String getFullName() {
        return getStringProperty("TwitterConnectSettings.fullName");
    }

    public void setId(String set) {
        setProperty("TwitterConnectSettings.id", set);
    }

    public String getId() {
        return getStringProperty("TwitterConnectSettings.id");
    }

    public void setToken(String set) {
        setProperty("TwitterConnectSettings.token", set);
    }

    public String getToken() {
        return getStringProperty("TwitterConnectSettings.token");
    }

    public void setTokenSecret(String set) {
        setProperty("TwitterConnectSettings.tokenSecret", set);
    }

    public String getTokenSecret() {
        return getStringProperty("TwitterConnectSettings.tokenSecret");
    }

    public void setSmallIconUrl(String set) {
        setProperty("TwitterConnectSettings.smallIconUrl", set);
    }

    public String getSmallIconUrl() {
        return getStringProperty("TwitterConnectSettings.smallIconUrl");
    }

    public void setLargeIconUrl(String set) {
        setProperty("TwitterConnectSettings.largeIconUrl", set);
    }

    public String getLargeIconUrl() {
        return getStringProperty("TwitterConnectSettings.largeIconUrl");
    }

    public void setIcon(android.graphics.Bitmap set) {
        setProperty("TwitterConnectSettings.icon", set);
    }

    public android.graphics.Bitmap getIcon() {
        return getBitmapProperty("TwitterConnectSettings.icon");
    }

}
