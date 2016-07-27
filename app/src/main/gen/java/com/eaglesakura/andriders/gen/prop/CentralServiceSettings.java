package com.eaglesakura.andriders.gen.prop;


public class CentralServiceSettings extends com.eaglesakura.android.property.internal.GeneratedProperties {
    
    public static final String ID_WIFIDISABLE = "CentralServiceSettings.wifiDisable";
    public static final String ID_CPUNOSLEEP = "CentralServiceSettings.cpuNoSleep";
    public static final String ID_PROXIMITYCOMMANDENABLE = "CentralServiceSettings.proximityCommandEnable";
    public static final String ID_PROXIMITYCOMMANDSCREENLINK = "CentralServiceSettings.proximityCommandScreenLink";
    public static final String ID_TIMERCOMMANDENABLE = "CentralServiceSettings.timerCommandEnable";
    public static final String ID_SPEEDCOMMANDENABLE = "CentralServiceSettings.speedCommandEnable";
    public static final String ID_DISTANCECOMMANDENABLE = "CentralServiceSettings.distanceCommandEnable";
    public static final String ID_GPSACCURACY = "CentralServiceSettings.gpsAccuracy";
    
    public CentralServiceSettings(){ }
    public CentralServiceSettings(com.eaglesakura.android.property.PropertyStore store) { setPropertyStore(store); }
    public void setWifiDisable(boolean set){ setProperty("CentralServiceSettings.wifiDisable", set); }
    public boolean isWifiDisable(){ return getBooleanProperty("CentralServiceSettings.wifiDisable"); }
    public void setCpuNoSleep(boolean set){ setProperty("CentralServiceSettings.cpuNoSleep", set); }
    public boolean isCpuNoSleep(){ return getBooleanProperty("CentralServiceSettings.cpuNoSleep"); }
    public void setProximityCommandEnable(boolean set){ setProperty("CentralServiceSettings.proximityCommandEnable", set); }
    public boolean isProximityCommandEnable(){ return getBooleanProperty("CentralServiceSettings.proximityCommandEnable"); }
    public void setProximityCommandScreenLink(boolean set){ setProperty("CentralServiceSettings.proximityCommandScreenLink", set); }
    public boolean isProximityCommandScreenLink(){ return getBooleanProperty("CentralServiceSettings.proximityCommandScreenLink"); }
    public void setTimerCommandEnable(boolean set){ setProperty("CentralServiceSettings.timerCommandEnable", set); }
    public boolean isTimerCommandEnable(){ return getBooleanProperty("CentralServiceSettings.timerCommandEnable"); }
    public void setSpeedCommandEnable(boolean set){ setProperty("CentralServiceSettings.speedCommandEnable", set); }
    public boolean isSpeedCommandEnable(){ return getBooleanProperty("CentralServiceSettings.speedCommandEnable"); }
    public void setDistanceCommandEnable(boolean set){ setProperty("CentralServiceSettings.distanceCommandEnable", set); }
    public boolean isDistanceCommandEnable(){ return getBooleanProperty("CentralServiceSettings.distanceCommandEnable"); }
    public void setGpsAccuracy(float set){ setProperty("CentralServiceSettings.gpsAccuracy", set); }
    public float getGpsAccuracy(){ return getFloatProperty("CentralServiceSettings.gpsAccuracy"); }
    
}
