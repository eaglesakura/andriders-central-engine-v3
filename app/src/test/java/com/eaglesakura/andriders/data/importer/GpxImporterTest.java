package com.eaglesakura.andriders.data.importer;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.data.gpx.GpxParser;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.util.DateUtil;

import org.junit.Test;

import java.io.File;
import java.util.TimeZone;

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

        SessionLogDatabase db = new SessionLogDatabase(getContext());

        fail("Test Not Updated");
//        try {
//            assertEquals(db.loadMaxSpeedKmh(), 61.0, 1.0);  // AACR最高速度
//            assertEquals(
//                    db.loadMaxSpeedKmh(importer.getImportStartDate().getTime(), importer.getImportEndDate().getTime()),
//                    db.loadMaxSpeedKmh(),
//                    0.01);  // AACR最高速度
//
//            SessionTotalCollection collection = db.loadTotal(SessionTotalCollection.Order.Asc);
//            assertNotNull(collection);
//            assertEquals(collection.getTotals().size(), 1);
//            assertEquals(collection.getSumDistanceKm(), 160, 10);
//            assertEquals(collection.getMaxSpeedKmh(), 61.0, 1.0);
//            assertEquals(collection.getLongestDateDistanceKm(), 160.0, 10);
//            assertTrue(collection.getRangeCalorie(importer.getImportStartDate().getTime(), importer.getImportEndDate().getTime()) > 2000);
//            assertTrue(collection.getRangeExercise(importer.getImportStartDate().getTime(), importer.getImportEndDate().getTime()) > 20);
//        } catch (Exception e) {
//
//        }
    }
}
