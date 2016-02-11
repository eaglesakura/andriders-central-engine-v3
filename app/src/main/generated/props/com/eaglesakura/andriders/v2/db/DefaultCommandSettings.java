package com.eaglesakura.andriders.v2.db;

import android.content.Context;

public class DefaultCommandSettings extends com.eaglesakura.android.db.BasePropertiesDatabase {

    public static final String ID_VIDEOMAXRECORDTIMEMS = "DefaultCommandSettings.videoMaxRecordTimeMs";

    public DefaultCommandSettings(Context context) {
        super(context, "props.db");
        _initialize();
    }

    public DefaultCommandSettings(Context context, String dbFileName) {
        super(context, dbFileName);
        _initialize();
    }

    protected void _initialize() {

        addProperty("DefaultCommandSettings.videoMaxRecordTimeMs", "600000");

        load();

    }

    public void setVideoMaxRecordTimeMs(int set) {
        setProperty("DefaultCommandSettings.videoMaxRecordTimeMs", set);
    }

    public int getVideoMaxRecordTimeMs() {
        return getIntProperty("DefaultCommandSettings.videoMaxRecordTimeMs");
    }

}
