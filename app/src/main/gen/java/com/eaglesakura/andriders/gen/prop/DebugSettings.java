package com.eaglesakura.andriders.gen.prop;


public class DebugSettings extends com.eaglesakura.android.property.internal.GeneratedProperties {
    
    public static final String ID_DEBUGENABLE = "DebugSettings.debugEnable";
    
    public DebugSettings(){ }
    public DebugSettings(com.eaglesakura.android.property.PropertyStore store) { setPropertyStore(store); }
    public void setDebugEnable(boolean set){ setProperty("DebugSettings.debugEnable", set); }
    public boolean isDebugEnable(){ return getBooleanProperty("DebugSettings.debugEnable"); }
    
}
