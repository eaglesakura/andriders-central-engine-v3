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
 * DAO for table "DB_COMMAND".
*/
public class DbCommandDao extends AbstractDao<DbCommand, String> {

    public static final String TABLENAME = "DB_COMMAND";

    /**
     * Properties of entity DbCommand.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property CommandKey = new Property(0, String.class, "commandKey", true, "COMMAND_KEY");
        public final static Property Category = new Property(1, int.class, "category", false, "CATEGORY");
        public final static Property PackageName = new Property(2, String.class, "packageName", false, "PACKAGE_NAME");
        public final static Property IconPng = new Property(3, byte[].class, "iconPng", false, "ICON_PNG");
        public final static Property CommandData = new Property(4, String.class, "commandData", false, "COMMAND_DATA");
        public final static Property IntentData = new Property(5, String.class, "intentData", false, "INTENT_DATA");
    };


    public DbCommandDao(DaoConfig config) {
        super(config);
    }
    
    public DbCommandDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_COMMAND\" (" + //
                "\"COMMAND_KEY\" TEXT PRIMARY KEY NOT NULL UNIQUE ," + // 0: commandKey
                "\"CATEGORY\" INTEGER NOT NULL ," + // 1: category
                "\"PACKAGE_NAME\" TEXT NOT NULL ," + // 2: packageName
                "\"ICON_PNG\" BLOB NOT NULL ," + // 3: iconPng
                "\"COMMAND_DATA\" TEXT," + // 4: commandData
                "\"INTENT_DATA\" TEXT);"); // 5: intentData
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_DB_COMMAND_CATEGORY ON DB_COMMAND" +
                " (\"CATEGORY\");");
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_COMMAND\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbCommand entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getCommandKey());
        stmt.bindLong(2, entity.getCategory());
        stmt.bindString(3, entity.getPackageName());
        stmt.bindBlob(4, entity.getIconPng());
 
        String commandData = entity.getCommandData();
        if (commandData != null) {
            stmt.bindString(5, commandData);
        }
 
        String intentData = entity.getIntentData();
        if (intentData != null) {
            stmt.bindString(6, intentData);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbCommand entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getCommandKey());
        stmt.bindLong(2, entity.getCategory());
        stmt.bindString(3, entity.getPackageName());
        stmt.bindBlob(4, entity.getIconPng());
 
        String commandData = entity.getCommandData();
        if (commandData != null) {
            stmt.bindString(5, commandData);
        }
 
        String intentData = entity.getIntentData();
        if (intentData != null) {
            stmt.bindString(6, intentData);
        }
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.getString(offset + 0);
    }    

    @Override
    public DbCommand readEntity(Cursor cursor, int offset) {
        DbCommand entity = new DbCommand( //
            cursor.getString(offset + 0), // commandKey
            cursor.getInt(offset + 1), // category
            cursor.getString(offset + 2), // packageName
            cursor.getBlob(offset + 3), // iconPng
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // commandData
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5) // intentData
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbCommand entity, int offset) {
        entity.setCommandKey(cursor.getString(offset + 0));
        entity.setCategory(cursor.getInt(offset + 1));
        entity.setPackageName(cursor.getString(offset + 2));
        entity.setIconPng(cursor.getBlob(offset + 3));
        entity.setCommandData(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setIntentData(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
     }
    
    @Override
    protected final String updateKeyAfterInsert(DbCommand entity, long rowId) {
        return entity.getCommandKey();
    }
    
    @Override
    public String getKey(DbCommand entity) {
        if(entity != null) {
            return entity.getCommandKey();
        } else {
            return null;
        }
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
