package com.eaglesakura.andriders.v2.db;

import android.content.Context;

public class DebugSettings extends com.eaglesakura.android.db.BasePropertiesDatabase {

    public static final String ID_DEBUGENABLE = "DebugSettings.debugEnable";
    public static final String ID_ACESRENDERDEBUGINFO = "DebugSettings.acesRenderDebugInfo";
    public static final String ID_RENDERLOCATION = "DebugSettings.renderLocation";

    public DebugSettings(Context context) {
        super(context, "props.db");
        _initialize();
    }

    public DebugSettings(Context context, String dbFileName) {
        super(context, dbFileName);
        _initialize();
    }

    protected void _initialize() {

        addProperty("DebugSettings.debugEnable", "false");
        addProperty("DebugSettings.acesRenderDebugInfo", "false");
        addProperty("DebugSettings.renderLocation", "false");

        load();

    }

    public void setDebugEnable(boolean set) {
        setProperty("DebugSettings.debugEnable", set);
    }

    public boolean getDebugEnable() {
        return getBooleanProperty("DebugSettings.debugEnable");
    }

    public void setAcesRenderDebugInfo(boolean set) {
        setProperty("DebugSettings.acesRenderDebugInfo", set);
    }

    public boolean getAcesRenderDebugInfo() {
        return getBooleanProperty("DebugSettings.acesRenderDebugInfo");
    }

    public void setRenderLocation(boolean set) {
        setProperty("DebugSettings.renderLocation", set);
    }

    public boolean getRenderLocation() {
        return getBooleanProperty("DebugSettings.renderLocation");
    }

}
