package com.eaglesakura.andriders.v2.db;

import android.content.Context;

public class CentralServiceSettings extends com.eaglesakura.android.db.BasePropertiesDatabase {

    public static final String ID_WIFIDISABLE = "CentralServiceSettings.wifiDisable";
    public static final String ID_CPUNOSLEEP = "CentralServiceSettings.cpuNoSleep";
    public static final String ID_ENABLEOLDSDKCOMPAT = "CentralServiceSettings.enableOldSdkCompat";
    public static final String ID_MEDIADIRECTORY1H = "CentralServiceSettings.mediaDirectory1h";
    public static final String ID_PROXIMITYCOMMANDENABLE = "CentralServiceSettings.proximityCommandEnable";
    public static final String ID_PROXIMITYCOMMANDSCREENLINK = "CentralServiceSettings.proximityCommandScreenLink";
    public static final String ID_TIMERCOMMANDENABLE = "CentralServiceSettings.timerCommandEnable";
    public static final String ID_SPEEDCOMMANDENABLE = "CentralServiceSettings.speedCommandEnable";
    public static final String ID_DISTANCECOMMANDENABLE = "CentralServiceSettings.distanceCommandEnable";
    public static final String ID_GPSENABLE = "CentralServiceSettings.gpsEnable";
    public static final String ID_GPSACCURACY = "CentralServiceSettings.gpsAccuracy";
    public static final String ID_SOUNDENABLE = "CentralServiceSettings.soundEnable";
    public static final String ID_CUSTOMSOUNDENABLE = "CentralServiceSettings.customSoundEnable";

    public CentralServiceSettings(Context context) {
        super(context, "props.db");
        _initialize();
    }

    public CentralServiceSettings(Context context, String dbFileName) {
        super(context, dbFileName);
        _initialize();
    }

    protected void _initialize() {

        addProperty("CentralServiceSettings.wifiDisable", "true");
        addProperty("CentralServiceSettings.cpuNoSleep", "true");
        addProperty("CentralServiceSettings.enableOldSdkCompat", "false");
        addProperty("CentralServiceSettings.mediaDirectory1h", "false");
        addProperty("CentralServiceSettings.proximityCommandEnable", "true");
        addProperty("CentralServiceSettings.proximityCommandScreenLink", "true");
        addProperty("CentralServiceSettings.timerCommandEnable", "true");
        addProperty("CentralServiceSettings.speedCommandEnable", "true");
        addProperty("CentralServiceSettings.distanceCommandEnable", "true");
        addProperty("CentralServiceSettings.gpsEnable", "true");
        addProperty("CentralServiceSettings.gpsAccuracy", "50.0");
        addProperty("CentralServiceSettings.soundEnable", "true");
        addProperty("CentralServiceSettings.customSoundEnable", "true");

        load();

    }

    public void setWifiDisable(boolean set) {
        setProperty("CentralServiceSettings.wifiDisable", set);
    }

    public boolean getWifiDisable() {
        return getBooleanProperty("CentralServiceSettings.wifiDisable");
    }

    public void setCpuNoSleep(boolean set) {
        setProperty("CentralServiceSettings.cpuNoSleep", set);
    }

    public boolean getCpuNoSleep() {
        return getBooleanProperty("CentralServiceSettings.cpuNoSleep");
    }

    public void setEnableOldSdkCompat(boolean set) {
        setProperty("CentralServiceSettings.enableOldSdkCompat", set);
    }

    public boolean getEnableOldSdkCompat() {
        return getBooleanProperty("CentralServiceSettings.enableOldSdkCompat");
    }

    public void setMediaDirectory1h(boolean set) {
        setProperty("CentralServiceSettings.mediaDirectory1h", set);
    }

    public boolean getMediaDirectory1h() {
        return getBooleanProperty("CentralServiceSettings.mediaDirectory1h");
    }

    public void setProximityCommandEnable(boolean set) {
        setProperty("CentralServiceSettings.proximityCommandEnable", set);
    }

    public boolean getProximityCommandEnable() {
        return getBooleanProperty("CentralServiceSettings.proximityCommandEnable");
    }

    public void setProximityCommandScreenLink(boolean set) {
        setProperty("CentralServiceSettings.proximityCommandScreenLink", set);
    }

    public boolean getProximityCommandScreenLink() {
        return getBooleanProperty("CentralServiceSettings.proximityCommandScreenLink");
    }

    public void setTimerCommandEnable(boolean set) {
        setProperty("CentralServiceSettings.timerCommandEnable", set);
    }

    public boolean getTimerCommandEnable() {
        return getBooleanProperty("CentralServiceSettings.timerCommandEnable");
    }

    public void setSpeedCommandEnable(boolean set) {
        setProperty("CentralServiceSettings.speedCommandEnable", set);
    }

    public boolean getSpeedCommandEnable() {
        return getBooleanProperty("CentralServiceSettings.speedCommandEnable");
    }

    public void setDistanceCommandEnable(boolean set) {
        setProperty("CentralServiceSettings.distanceCommandEnable", set);
    }

    public boolean getDistanceCommandEnable() {
        return getBooleanProperty("CentralServiceSettings.distanceCommandEnable");
    }

    public void setGpsEnable(boolean set) {
        setProperty("CentralServiceSettings.gpsEnable", set);
    }

    public boolean getGpsEnable() {
        return getBooleanProperty("CentralServiceSettings.gpsEnable");
    }

    public void setGpsAccuracy(float set) {
        setProperty("CentralServiceSettings.gpsAccuracy", set);
    }

    public float getGpsAccuracy() {
        return getFloatProperty("CentralServiceSettings.gpsAccuracy");
    }

    public void setSoundEnable(boolean set) {
        setProperty("CentralServiceSettings.soundEnable", set);
    }

    public boolean getSoundEnable() {
        return getBooleanProperty("CentralServiceSettings.soundEnable");
    }

    public void setCustomSoundEnable(boolean set) {
        setProperty("CentralServiceSettings.customSoundEnable", set);
    }

    public boolean getCustomSoundEnable() {
        return getBooleanProperty("CentralServiceSettings.customSoundEnable");
    }

}
