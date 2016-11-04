package com.eaglesakura.andriders.dao.central;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DB_BLE_FITNESS_DEVICE".
*/
public class DbBleFitnessDeviceDao extends AbstractDao<DbBleFitnessDevice, String> {

    public static final String TABLENAME = "DB_BLE_FITNESS_DEVICE";

    /**
     * Properties of entity DbBleFitnessDevice.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Address = new Property(0, String.class, "address", true, "ADDRESS");
        public final static Property SelectedCount = new Property(1, int.class, "selectedCount", false, "SELECTED_COUNT");
        public final static Property DeviceType = new Property(2, int.class, "deviceType", false, "DEVICE_TYPE");
    };


    public DbBleFitnessDeviceDao(DaoConfig config) {
        super(config);
    }
    
    public DbBleFitnessDeviceDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_BLE_FITNESS_DEVICE\" (" + //
                "\"ADDRESS\" TEXT PRIMARY KEY NOT NULL UNIQUE ," + // 0: address
                "\"SELECTED_COUNT\" INTEGER NOT NULL ," + // 1: selectedCount
                "\"DEVICE_TYPE\" INTEGER NOT NULL );"); // 2: deviceType
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_BLE_FITNESS_DEVICE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbBleFitnessDevice entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getAddress());
        stmt.bindLong(2, entity.getSelectedCount());
        stmt.bindLong(3, entity.getDeviceType());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbBleFitnessDevice entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getAddress());
        stmt.bindLong(2, entity.getSelectedCount());
        stmt.bindLong(3, entity.getDeviceType());
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.getString(offset + 0);
    }    

    @Override
    public DbBleFitnessDevice readEntity(Cursor cursor, int offset) {
        DbBleFitnessDevice entity = new DbBleFitnessDevice( //
            cursor.getString(offset + 0), // address
            cursor.getInt(offset + 1), // selectedCount
            cursor.getInt(offset + 2) // deviceType
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbBleFitnessDevice entity, int offset) {
        entity.setAddress(cursor.getString(offset + 0));
        entity.setSelectedCount(cursor.getInt(offset + 1));
        entity.setDeviceType(cursor.getInt(offset + 2));
     }
    
    @Override
    protected final String updateKeyAfterInsert(DbBleFitnessDevice entity, long rowId) {
        return entity.getAddress();
    }
    
    @Override
    public String getKey(DbBleFitnessDevice entity) {
        if(entity != null) {
            return entity.getAddress();
        } else {
            return null;
        }
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}