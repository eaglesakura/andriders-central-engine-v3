package com.eaglesakura.andriders.computer.central.record;

import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;

/**
 * ユーザーの自己新記録を管理する
 */
public class UserRecordCentral implements CentralDataManager.ICentral {
    @Override
    public void onUpdate(CentralDataManager parent) {

    }

    /**
     * FIXME データ生成をサポートする
     *
     * @param parent 呼び出し元
     * @param result 構築先
     */
    @Override
    public void buildData(CentralDataManager parent, RawCentralData result) {

    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}
