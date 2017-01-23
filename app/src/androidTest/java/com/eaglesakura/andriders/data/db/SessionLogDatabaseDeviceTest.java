package com.eaglesakura.andriders.data.db;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.andriders.storage.AppStorageManager;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Garnet;

import org.junit.Test;

import org.sqlite.database.SQLiteX;
import org.sqlite.database.sqlite.SQLiteDatabase;
import org.sqlite.database.sqlite.SQLiteOpenHelper;

import android.database.Cursor;

public class SessionLogDatabaseDeviceTest extends AppDeviceTestCase {

    @Override
    public void onSetup() {
        super.onSetup();
        Garnet.override(AppStorageProvider.class, AppStorageProvider.class);
        SQLiteX.install(getContext());
    }

    @Test
    public void JSONデータをパースする() throws Throwable {
        AppStorageManager storageManager = Garnet.instance(AppStorageProvider.class, AppStorageManager.class);

        SQLiteOpenHelper helper = new SQLiteOpenHelper(
                getContext(),
                storageManager.getExternalDatabasePath("v3_session_log.db").getAbsolutePath(),
                null,
                SessionLogDatabase.SUPPORTED_DATABASE_VERSION
        ) {
            @Override
            public void onCreate(SQLiteDatabase sqLiteDatabase) {
            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
                fail();
            }
        };

        SQLiteDatabase db = helper.getReadableDatabase();
        assertNotNull(db);

        final String RAW_QUERY = "" +
                "SELECT\n" +
                "    DATE,\n" +
                "    SESSION_ID,\n" +
//                "    CENTRAL_JSON,\n" +
                "    json_extract(CENTRAL_JSON, \"$.example.errorInt\") as json_int,\n" +
                "    json_extract(CENTRAL_JSON, \"$.example.errorStr\") as json_str,\n" +
                "    json_extract(CENTRAL_JSON, \"$.example.errorNum\") as json_number,\n" +
                "    json_extract(CENTRAL_JSON, \"$.sensor.location.latitude\") as json_number,\n" +
                "    json_extract(CENTRAL_JSON, \"$.sensor.location.longitude\") as json_number\n" +
                "FROM\n" +
                "    DB_SESSION_POINT\n" +
                "ORDER BY\n" +
                "    DATE ASC";

        Cursor cursor = db.rawQuery(RAW_QUERY, null);
        assertNotNull(cursor);

        try {
            validate(cursor.getCount()).from(1);
            assertTrue(cursor.moveToFirst());
            do {
                int row = 0;
                String DATE = cursor.getString(row++);
                String SESSION_ID = cursor.getString(row++);
                String ERROR_INT = cursor.getString(row++);
                String ERROR_STR = cursor.getString(row++);
                String ERROR_NUMBER = cursor.getString(row++);
                String LOC_LATITUDE = cursor.getString(row++);
                String LOC_LONGITUDE = cursor.getString(row++);

                assertNotEmpty(DATE);
                assertNotEmpty(SESSION_ID);
                assertNull(ERROR_INT);
                assertNull(ERROR_STR);
                assertNull(ERROR_NUMBER);
//                assertNotEmpty(LOC_LATITUDE);
//                assertNotEmpty(LOC_LONGITUDE);

                AppLog.db("Extract Date[%s] lat[%s] lng[%s]", DATE, LOC_LATITUDE, LOC_LONGITUDE);
            } while (cursor.moveToNext());
        } finally {
            cursor.close();
            db.close();
        }
    }
}