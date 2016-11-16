package com.eaglesakura.andriders.data.sensor;

import com.google.android.gms.fitness.data.BleDevice;

import com.eaglesakura.andriders.data.db.CentralSettingDatabase;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.model.ble.BleDeviceCache;
import com.eaglesakura.andriders.model.ble.BleDeviceCacheCollection;
import com.eaglesakura.andriders.model.ble.BleDeviceType;
import com.eaglesakura.andriders.system.manager.CentralSettingManager;
import com.eaglesakura.android.garnet.Singleton;

import org.greenrobot.greendao.annotation.NotNull;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * スキャン済みのデバイスキャッシュ等を扱う
 */
@Singleton
public class SensorDataManager extends CentralSettingManager {
    public SensorDataManager(@NotNull Context context) {
        super(context);
    }

    BleDeviceCache save(@NonNull BleDeviceCache cache) {
        try (CentralSettingDatabase db = open()) {
            db.save(cache);
        }
        return cache;
    }

    /**
     * スキャンされたBLEデバイス情報を保存する
     *
     * @param rawDevice Google Fitにて保存されたデータ
     * @return 生成されたキャッシュ情報
     */
    @NonNull
    public BleDeviceCache save(@NonNull BleDevice rawDevice) throws AppException {
        return save(new BleDeviceCache(rawDevice));
    }

    /**
     * スキャン済みのすべてのデバイスをロードする
     *
     * @param type デバイス種別
     * @return デバイス一覧, 見つからない場合は空リスト
     */
    @NonNull
    public BleDeviceCacheCollection load(@NonNull BleDeviceType type) throws AppException {
        try (CentralSettingDatabase db = open()) {
            return db.listBleDevices(type.getDeviceTypeId());
        }
    }
}
