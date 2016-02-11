package com.eaglesakura.andriders.computer.central.record;

import com.eaglesakura.andriders.computer.central.CentralDataManager;

/**
 * ユーザーの自己新記録を管理する
 */
public class UserRecordCentral implements CentralDataManager.ICentral {
    @Override
    public void onUpdate(CentralDataManager parent) {

    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}
