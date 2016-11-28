package com.eaglesakura.andriders.v3.gen.config;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import java.util.HashMap;

public class AppStatusConfig {
    
    FirebaseRemoteConfig mRemoteConfig;
    
    public static final String ID_DATABASE_PATH_CONFIG = "database_path_config";
    
    public AppStatusConfig(){
    
            
    }
    private synchronized FirebaseRemoteConfig getRemoteConfig() {
    
            
        if (mRemoteConfig == null) {
                    
            mRemoteConfig = FirebaseRemoteConfig.getInstance();
            
            HashMap<String, Object> defValues = new HashMap<>();
            defValues.put("database_path_config", "nil");
            mRemoteConfig.setDefaults(defValues);
            
        }
        return mRemoteConfig;
        
    }
    public String getDatabasePathConfig(){ return getRemoteConfig().getString("database_path_config"); }
    
}
