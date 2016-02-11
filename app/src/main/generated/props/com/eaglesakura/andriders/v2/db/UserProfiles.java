package com.eaglesakura.andriders.v2.db;

import android.content.Context;

public class UserProfiles extends com.eaglesakura.android.db.BasePropertiesDatabase {

    public static final String ID_USERWEIGHT = "UserProfiles.userWeight";
    public static final String ID_NORMALHEARTRATE = "UserProfiles.normalHeartrate";
    public static final String ID_MAXHEARTRATE = "UserProfiles.maxHeartrate";
    public static final String ID_WHEELOUTERLENGTH = "UserProfiles.wheelOuterLength";
    public static final String ID_SPEEDZONECRUISE = "UserProfiles.speedZoneCruise";
    public static final String ID_SPEEDZONESPRINT = "UserProfiles.speedZoneSprint";
    public static final String ID_CADENCEZONEIDEAL = "UserProfiles.cadenceZoneIdeal";
    public static final String ID_CADENCEZONEHIGH = "UserProfiles.cadenceZoneHigh";
    public static final String ID_GPSENABLE = "UserProfiles.gpsEnable";
    public static final String ID_BLEHEARTRATEMONITORADDRESS = "UserProfiles.bleHeartrateMonitorAddress";
    public static final String ID_BLESPEEDCADENCESENSORADDRESS = "UserProfiles.bleSpeedCadenceSensorAddress";
    public static final String ID_BTREMOTECENTRALADDRESS = "UserProfiles.btRemoteCentralAddress";
    public static final String ID_CYCLECOMPUTERINTERFACEENABLE = "UserProfiles.cycleComputerInterfaceEnable";

    public UserProfiles(Context context) {
        super(context, "props.db");
        _initialize();
    }

    public UserProfiles(Context context, String dbFileName) {
        super(context, dbFileName);
        _initialize();
    }

    protected void _initialize() {

        addProperty("UserProfiles.userWeight", "65.0");
        addProperty("UserProfiles.normalHeartrate", "70");
        addProperty("UserProfiles.maxHeartrate", "190");
        addProperty("UserProfiles.wheelOuterLength", "2096");
        addProperty("UserProfiles.speedZoneCruise", "25");
        addProperty("UserProfiles.speedZoneSprint", "35");
        addProperty("UserProfiles.cadenceZoneIdeal", "80");
        addProperty("UserProfiles.cadenceZoneHigh", "100");
        addProperty("UserProfiles.gpsEnable", "true");
        addProperty("UserProfiles.bleHeartrateMonitorAddress", "");
        addProperty("UserProfiles.bleSpeedCadenceSensorAddress", "");
        addProperty("UserProfiles.btRemoteCentralAddress", "");
        addProperty("UserProfiles.cycleComputerInterfaceEnable", "true");

        load();

    }

    public void setUserWeight(double set) {
        setProperty("UserProfiles.userWeight", set);
    }

    public double getUserWeight() {
        return getDoubleProperty("UserProfiles.userWeight");
    }

    public void setNormalHeartrate(int set) {
        setProperty("UserProfiles.normalHeartrate", set);
    }

    public int getNormalHeartrate() {
        return getIntProperty("UserProfiles.normalHeartrate");
    }

    public void setMaxHeartrate(int set) {
        setProperty("UserProfiles.maxHeartrate", set);
    }

    public int getMaxHeartrate() {
        return getIntProperty("UserProfiles.maxHeartrate");
    }

    public void setWheelOuterLength(int set) {
        setProperty("UserProfiles.wheelOuterLength", set);
    }

    public int getWheelOuterLength() {
        return getIntProperty("UserProfiles.wheelOuterLength");
    }

    public void setSpeedZoneCruise(int set) {
        setProperty("UserProfiles.speedZoneCruise", set);
    }

    public int getSpeedZoneCruise() {
        return getIntProperty("UserProfiles.speedZoneCruise");
    }

    public void setSpeedZoneSprint(int set) {
        setProperty("UserProfiles.speedZoneSprint", set);
    }

    public int getSpeedZoneSprint() {
        return getIntProperty("UserProfiles.speedZoneSprint");
    }

    public void setCadenceZoneIdeal(int set) {
        setProperty("UserProfiles.cadenceZoneIdeal", set);
    }

    public int getCadenceZoneIdeal() {
        return getIntProperty("UserProfiles.cadenceZoneIdeal");
    }

    public void setCadenceZoneHigh(int set) {
        setProperty("UserProfiles.cadenceZoneHigh", set);
    }

    public int getCadenceZoneHigh() {
        return getIntProperty("UserProfiles.cadenceZoneHigh");
    }

    public void setGpsEnable(boolean set) {
        setProperty("UserProfiles.gpsEnable", set);
    }

    public boolean getGpsEnable() {
        return getBooleanProperty("UserProfiles.gpsEnable");
    }

    public void setBleHeartrateMonitorAddress(String set) {
        setProperty("UserProfiles.bleHeartrateMonitorAddress", set);
    }

    public String getBleHeartrateMonitorAddress() {
        return getStringProperty("UserProfiles.bleHeartrateMonitorAddress");
    }

    public void setBleSpeedCadenceSensorAddress(String set) {
        setProperty("UserProfiles.bleSpeedCadenceSensorAddress", set);
    }

    public String getBleSpeedCadenceSensorAddress() {
        return getStringProperty("UserProfiles.bleSpeedCadenceSensorAddress");
    }

    public void setBtRemoteCentralAddress(String set) {
        setProperty("UserProfiles.btRemoteCentralAddress", set);
    }

    public String getBtRemoteCentralAddress() {
        return getStringProperty("UserProfiles.btRemoteCentralAddress");
    }

    public void setCycleComputerInterfaceEnable(boolean set) {
        setProperty("UserProfiles.cycleComputerInterfaceEnable", set);
    }

    public boolean getCycleComputerInterfaceEnable() {
        return getBooleanProperty("UserProfiles.cycleComputerInterfaceEnable");
    }

}
