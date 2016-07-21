package com.eaglesakura.andriders.dao.session;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DB_SESSION_POINT".
*/
public class DbSessionPointDao extends AbstractDao<DbSessionPoint, java.util.Date> {

    public static final String TABLENAME = "DB_SESSION_POINT";

    /**
     * Properties of entity DbSessionPoint.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Date = new Property(0, java.util.Date.class, "date", true, "DATE");
        public final static Property Central = new Property(1, byte[].class, "central", false, "CENTRAL");
    };


    public DbSessionPointDao(DaoConfig config) {
        super(config);
    }
    
    public DbSessionPointDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_SESSION_POINT\" (" + //
                "\"DATE\" INTEGER PRIMARY KEY NOT NULL UNIQUE ," + // 0: date
                "\"CENTRAL\" BLOB NOT NULL );"); // 1: central
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_SESSION_POINT\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbSessionPoint entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getDate().getTime());
        stmt.bindBlob(2, entity.getCentral());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbSessionPoint entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getDate().getTime());
        stmt.bindBlob(2, entity.getCentral());
    }

    @Override
    public java.util.Date readKey(Cursor cursor, int offset) {
        return new java.util.Date(cursor.getLong(offset + 0));
    }    

    @Override
    public DbSessionPoint readEntity(Cursor cursor, int offset) {
        DbSessionPoint entity = new DbSessionPoint( //
            new java.util.Date(cursor.getLong(offset + 0)), // date
            cursor.getBlob(offset + 1) // central
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbSessionPoint entity, int offset) {
        entity.setDate(new java.util.Date(cursor.getLong(offset + 0)));
        entity.setCentral(cursor.getBlob(offset + 1));
     }
    
    @Override
    protected final java.util.Date updateKeyAfterInsert(DbSessionPoint entity, long rowId) {
        return entity.getDate();
    }
    
    @Override
    public java.util.Date getKey(DbSessionPoint entity) {
        if(entity != null) {
            return entity.getDate();
        } else {
            return null;
        }
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
