package com.eaglesakura.andriders.db.importer;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.central.log.SessionLogger;
import com.eaglesakura.andriders.data.gpx.GpxParser;
import com.eaglesakura.andriders.db.session.SessionLogDatabase;
import com.eaglesakura.util.DateUtil;

import org.junit.Test;

import java.io.File;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class GpxImporterTest extends AppUnitTestCase {

    @Test
    public void AACR2015のテストデータをインストールする() throws Exception {
        GpxImporter importer = new GpxImporter(getContext(), new File("../sdk/src/test/assets/gpx/sample-aacr2015.gpx").getAbsoluteFile());
        importer.getParser().setDateOption(GpxParser.DateOption.AddTimeZone);
        importer.install(() -> false);

        assertEquals(DateUtil.getYear(importer.getImportStartDate(), TimeZone.getDefault()), 2015);
        assertEquals(DateUtil.getMonth(importer.getImportStartDate(), TimeZone.getDefault()), 5);
        assertEquals(DateUtil.getDay(importer.getImportStartDate(), TimeZone.getDefault()), 24);
        assertEquals(DateUtil.getHour(importer.getImportStartDate(), TimeZone.getDefault()), 5);

        assertEquals(DateUtil.getYear(importer.getImportEndDate(), TimeZone.getDefault()), 2015);
        assertEquals(DateUtil.getMonth(importer.getImportEndDate(), TimeZone.getDefault()), 5);
        assertEquals(DateUtil.getDay(importer.getImportEndDate(), TimeZone.getDefault()), 24);
        assertEquals(DateUtil.getHour(importer.getImportEndDate(), TimeZone.getDefault()), 16);

        SessionLogDatabase db = new SessionLogDatabase(getContext(), mStorageManager.getDatabasePath(SessionLogger.DATABASE_NAME));
        try {
            assertEquals(db.loadMaxSpeedKmh(), 61.0, 1.0);  // AACR最高速度
            assertEquals(
                    db.loadMaxSpeedKmh(importer.getImportStartDate().getTime(), importer.getImportEndDate().getTime()),
                    db.loadMaxSpeedKmh(),
                    0.01);  // AACR最高速度
        } catch (Exception e) {

        }
    }
}
