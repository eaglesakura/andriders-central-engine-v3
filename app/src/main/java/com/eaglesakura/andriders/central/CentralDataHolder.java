package com.eaglesakura.andriders.central;

import com.eaglesakura.andriders.central.data.CentralDataManager;

/**
 * CentralDataの管理側を構築する
 */
public interface CentralDataHolder {
    CentralDataManager getCentralDataManager();
}
