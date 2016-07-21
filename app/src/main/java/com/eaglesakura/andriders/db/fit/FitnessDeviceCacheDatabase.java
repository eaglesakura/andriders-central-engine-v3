package com.eaglesakura.andriders.db.fit;

import com.google.android.gms.fitness.data.BleDevice;

import com.eaglesakura.andriders.dao.bledevice.DaoMaster;
import com.eaglesakura.andriders.dao.bledevice.DaoSession;
import com.eaglesakura.andriders.dao.bledevice.DbBleFitnessDevice;
import com.eaglesakura.andriders.dao.bledevice.DbBleFitnessDeviceDao;
import com.eaglesakura.andriders.google.FitnessDeviceType;
import com.eaglesakura.android.db.DaoDatabase;

import org.greenrobot.greendao.database.StandardDatabase;
import org.greenrobot.greendao.query.QueryBuilder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.List;

/**
 * 一度検索されたFitnessデバイスを管理する
 */
public class FitnessDeviceCacheDatabase extends DaoDatabase<DaoSession> {

    private static final int SUPPORTED_DATABASE_VERSION = 0x01;

    public FitnessDeviceCacheDatabase(Context context) {
        super(context, DaoMaster.class);
    }

    @Override
    protected SQLiteOpenHelper createHelper() {
        return new SQLiteOpenHelper(context, "fitnessdevice.db", null, SUPPORTED_DATABASE_VERSION) {

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                DaoMaster.createAllTables(new StandardDatabase(db), false);
            }
        };
    }

    /**
     * スキャン済みのデバイスを取得する
     */
    public List<DbBleFitnessDevice> listScanDevices(FitnessDeviceType device) {
        QueryBuilder<DbBleFitnessDevice> queryBuilder = session.getDbBleFitnessDeviceDao().queryBuilder();
        return queryBuilder
                .where(DbBleFitnessDeviceDao.Properties.DeviceType.eq(device.getDeviceTypeId()))
                .orderAsc(DbBleFitnessDeviceDao.Properties.FirstFoundDate)
                .list();
    }

    /**
     * アドレスを指定して取得する
     */
    public DbBleFitnessDevice load(String address) {
        return session.getDbBleFitnessDeviceDao().load(address);
    }

    /**
     * 情報を更新する
     */
    public void update(DbBleFitnessDevice device) {
        session.getDbBleFitnessDeviceDao().update(device);
    }

    /**
     * デバイスを検出した
     */
    public void foundDevice(FitnessDeviceType type, BleDevice device) {
        DbBleFitnessDevice dbDevice = load(device.getAddress());
        if (dbDevice == null) {
            dbDevice = new DbBleFitnessDevice();
            dbDevice.setDeviceType(type.getDeviceTypeId());
            dbDevice.setAddress(device.getAddress());
            dbDevice.setName(device.getName());
            dbDevice.setNameOrigin(device.getName());
            dbDevice.setFirstFoundDate(new Date());

            session.getDbBleFitnessDeviceDao().insert(dbDevice);
        }
    }

}
