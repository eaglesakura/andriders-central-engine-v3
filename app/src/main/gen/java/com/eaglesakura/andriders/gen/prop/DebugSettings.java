package com.eaglesakura.andriders.gen.prop;


public class DebugSettings extends com.eaglesakura.sloth.db.property.internal.GeneratedProperties {
    
    public static final String ID_DEBUGENABLE = "DebugSettings.debugEnable";
    
    public DebugSettings(){ }
    public DebugSettings(com.eaglesakura.sloth.db.property.PropertyStore store){ setPropertyStore(store); }
    public void setDebugEnable(boolean set){ setProperty("DebugSettings.debugEnable", set); }
    public boolean isDebugEnable(){ return getBooleanProperty("DebugSettings.debugEnable"); }
    
}
