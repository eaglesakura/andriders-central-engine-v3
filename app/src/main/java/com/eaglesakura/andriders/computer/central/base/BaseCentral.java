package com.eaglesakura.andriders.computer.central.base;

import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.db.Settings;

public abstract class BaseCentral implements CentralDataManager.ICentral {

    /**
     * 設定を取得する
     */
    protected Settings getSettings() {
        return Settings.getInstance();
    }
}
