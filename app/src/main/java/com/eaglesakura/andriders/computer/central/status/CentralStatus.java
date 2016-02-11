package com.eaglesakura.andriders.computer.central.status;

import com.eaglesakura.andriders.computer.central.CentralDataManager;

import android.content.Context;

/**
 * ACEのステータス管理を行う
 */
public class CentralStatus implements CentralDataManager.ICentral {
    final Context context;

    public CentralStatus(Context context) {
        this.context = context;
    }

    @Override
    public void onUpdate(CentralDataManager parent) {

    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}
