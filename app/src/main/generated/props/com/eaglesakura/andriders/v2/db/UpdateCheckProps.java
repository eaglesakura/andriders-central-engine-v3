package com.eaglesakura.andriders.v2.db;

import android.content.Context;

public class UpdateCheckProps extends com.eaglesakura.android.db.BasePropertiesDatabase {

    public static final String ID_INITIALIZERELEASED = "UpdateCheckProps.initializeReleased";

    public UpdateCheckProps(Context context) {
        super(context, "props.db");
        _initialize();
    }

    public UpdateCheckProps(Context context, String dbFileName) {
        super(context, dbFileName);
        _initialize();
    }

    protected void _initialize() {

        addProperty("UpdateCheckProps.initializeReleased", "0");

        load();

    }

    public void setInitializeReleased(int set) {
        setProperty("UpdateCheckProps.initializeReleased", set);
    }

    public int getInitializeReleased() {
        return getIntProperty("UpdateCheckProps.initializeReleased");
    }

}
