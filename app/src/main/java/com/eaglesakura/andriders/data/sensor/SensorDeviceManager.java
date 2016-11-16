package com.eaglesakura.andriders.data.sensor;

import com.eaglesakura.andriders.system.manager.CentralSettingManager;

import org.greenrobot.greendao.annotation.NotNull;

import android.content.Context;

/**
 * BLE等のセンサー設定管理を行う
 */
public class SensorDeviceManager extends CentralSettingManager {
    public SensorDeviceManager(@NotNull Context context) {
        super(context);
    }
}
