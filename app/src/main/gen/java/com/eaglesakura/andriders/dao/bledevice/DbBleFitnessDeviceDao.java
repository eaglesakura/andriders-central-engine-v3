package com.eaglesakura.andriders.dao.bledevice;

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
        public final static Property Name = new Property(1, String.class, "name", false, "NAME");
        public final static Property NameOrigin = new Property(2, String.class, "nameOrigin", false, "NAME_ORIGIN");
        public final static Property SelectedCount = new Property(3, int.class, "selectedCount", false, "SELECTED_COUNT");
        public final static Property FirstFoundDate = new Property(4, java.util.Date.class, "firstFoundDate", false, "FIRST_FOUND_DATE");
        public final static Property DeviceType = new Property(5, int.class, "deviceType", false, "DEVICE_TYPE");
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
                "\"NAME\" TEXT NOT NULL ," + // 1: name
                "\"NAME_ORIGIN\" TEXT NOT NULL ," + // 2: nameOrigin
                "\"SELECTED_COUNT\" INTEGER NOT NULL ," + // 3: selectedCount
                "\"FIRST_FOUND_DATE\" INTEGER NOT NULL ," + // 4: firstFoundDate
                "\"DEVICE_TYPE\" INTEGER NOT NULL );"); // 5: deviceType
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_DB_BLE_FITNESS_DEVICE_FIRST_FOUND_DATE ON DB_BLE_FITNESS_DEVICE" +
                " (\"FIRST_FOUND_DATE\");");
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
        stmt.bindString(2, entity.getName());
        stmt.bindString(3, entity.getNameOrigin());
        stmt.bindLong(4, entity.getSelectedCount());
        stmt.bindLong(5, entity.getFirstFoundDate().getTime());
        stmt.bindLong(6, entity.getDeviceType());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbBleFitnessDevice entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getAddress());
        stmt.bindString(2, entity.getName());
        stmt.bindString(3, entity.getNameOrigin());
        stmt.bindLong(4, entity.getSelectedCount());
        stmt.bindLong(5, entity.getFirstFoundDate().getTime());
        stmt.bindLong(6, entity.getDeviceType());
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.getString(offset + 0);
    }    

    @Override
    public DbBleFitnessDevice readEntity(Cursor cursor, int offset) {
        DbBleFitnessDevice entity = new DbBleFitnessDevice( //
            cursor.getString(offset + 0), // address
            cursor.getString(offset + 1), // name
            cursor.getString(offset + 2), // nameOrigin
            cursor.getInt(offset + 3), // selectedCount
            new java.util.Date(cursor.getLong(offset + 4)), // firstFoundDate
            cursor.getInt(offset + 5) // deviceType
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbBleFitnessDevice entity, int offset) {
        entity.setAddress(cursor.getString(offset + 0));
        entity.setName(cursor.getString(offset + 1));
        entity.setNameOrigin(cursor.getString(offset + 2));
        entity.setSelectedCount(cursor.getInt(offset + 3));
        entity.setFirstFoundDate(new java.util.Date(cursor.getLong(offset + 4)));
        entity.setDeviceType(cursor.getInt(offset + 5));
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