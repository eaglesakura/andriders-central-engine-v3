package com.eaglesakura.andriders.dao.session;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.eaglesakura.andriders.dao.session.DbSessionLog;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table DB_SESSION_LOG.
*/
public class DbSessionLogDao extends AbstractDao<DbSessionLog, String> {

    public static final String TABLENAME = "DB_SESSION_LOG";

    /**
     * Properties of entity DbSessionLog.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property SessionId = new Property(0, String.class, "sessionId", true, "SESSION_ID");
        public final static Property StartTime = new Property(1, java.util.Date.class, "startTime", false, "START_TIME");
        public final static Property EndTime = new Property(2, java.util.Date.class, "endTime", false, "END_TIME");
        public final static Property ActiveTimeMs = new Property(3, long.class, "activeTimeMs", false, "ACTIVE_TIME_MS");
        public final static Property MaxSpeedKmh = new Property(4, double.class, "maxSpeedKmh", false, "MAX_SPEED_KMH");
        public final static Property MaxCadence = new Property(5, int.class, "maxCadence", false, "MAX_CADENCE");
        public final static Property MaxHeartrate = new Property(6, int.class, "maxHeartrate", false, "MAX_HEARTRATE");
        public final static Property SumAltitude = new Property(7, double.class, "sumAltitude", false, "SUM_ALTITUDE");
        public final static Property SumDistanceKm = new Property(8, double.class, "sumDistanceKm", false, "SUM_DISTANCE_KM");
        public final static Property Calories = new Property(9, double.class, "calories", false, "CALORIES");
        public final static Property Exercise = new Property(10, double.class, "exercise", false, "EXERCISE");
    };


    public DbSessionLogDao(DaoConfig config) {
        super(config);
    }
    
    public DbSessionLogDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'DB_SESSION_LOG' (" + //
                "'SESSION_ID' TEXT PRIMARY KEY NOT NULL UNIQUE ," + // 0: sessionId
                "'START_TIME' INTEGER NOT NULL ," + // 1: startTime
                "'END_TIME' INTEGER NOT NULL ," + // 2: endTime
                "'ACTIVE_TIME_MS' INTEGER NOT NULL ," + // 3: activeTimeMs
                "'MAX_SPEED_KMH' REAL NOT NULL ," + // 4: maxSpeedKmh
                "'MAX_CADENCE' INTEGER NOT NULL ," + // 5: maxCadence
                "'MAX_HEARTRATE' INTEGER NOT NULL ," + // 6: maxHeartrate
                "'SUM_ALTITUDE' REAL NOT NULL ," + // 7: sumAltitude
                "'SUM_DISTANCE_KM' REAL NOT NULL ," + // 8: sumDistanceKm
                "'CALORIES' REAL NOT NULL ," + // 9: calories
                "'EXERCISE' REAL NOT NULL );"); // 10: exercise
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_DB_SESSION_LOG_ACTIVE_TIME_MS ON DB_SESSION_LOG" +
                " (ACTIVE_TIME_MS);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_DB_SESSION_LOG_MAX_SPEED_KMH ON DB_SESSION_LOG" +
                " (MAX_SPEED_KMH);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_DB_SESSION_LOG_SUM_DISTANCE_KM ON DB_SESSION_LOG" +
                " (SUM_DISTANCE_KM);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'DB_SESSION_LOG'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, DbSessionLog entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getSessionId());
        stmt.bindLong(2, entity.getStartTime().getTime());
        stmt.bindLong(3, entity.getEndTime().getTime());
        stmt.bindLong(4, entity.getActiveTimeMs());
        stmt.bindDouble(5, entity.getMaxSpeedKmh());
        stmt.bindLong(6, entity.getMaxCadence());
        stmt.bindLong(7, entity.getMaxHeartrate());
        stmt.bindDouble(8, entity.getSumAltitude());
        stmt.bindDouble(9, entity.getSumDistanceKm());
        stmt.bindDouble(10, entity.getCalories());
        stmt.bindDouble(11, entity.getExercise());
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public DbSessionLog readEntity(Cursor cursor, int offset) {
        DbSessionLog entity = new DbSessionLog( //
            cursor.getString(offset + 0), // sessionId
            new java.util.Date(cursor.getLong(offset + 1)), // startTime
            new java.util.Date(cursor.getLong(offset + 2)), // endTime
            cursor.getLong(offset + 3), // activeTimeMs
            cursor.getDouble(offset + 4), // maxSpeedKmh
            cursor.getInt(offset + 5), // maxCadence
            cursor.getInt(offset + 6), // maxHeartrate
            cursor.getDouble(offset + 7), // sumAltitude
            cursor.getDouble(offset + 8), // sumDistanceKm
            cursor.getDouble(offset + 9), // calories
            cursor.getDouble(offset + 10) // exercise
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, DbSessionLog entity, int offset) {
        entity.setSessionId(cursor.getString(offset + 0));
        entity.setStartTime(new java.util.Date(cursor.getLong(offset + 1)));
        entity.setEndTime(new java.util.Date(cursor.getLong(offset + 2)));
        entity.setActiveTimeMs(cursor.getLong(offset + 3));
        entity.setMaxSpeedKmh(cursor.getDouble(offset + 4));
        entity.setMaxCadence(cursor.getInt(offset + 5));
        entity.setMaxHeartrate(cursor.getInt(offset + 6));
        entity.setSumAltitude(cursor.getDouble(offset + 7));
        entity.setSumDistanceKm(cursor.getDouble(offset + 8));
        entity.setCalories(cursor.getDouble(offset + 9));
        entity.setExercise(cursor.getDouble(offset + 10));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(DbSessionLog entity, long rowId) {
        return entity.getSessionId();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(DbSessionLog entity) {
        if(entity != null) {
            return entity.getSessionId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
