package com.eaglesakura.andriders.db.importer;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.data.gpx.GpxParser;
import com.eaglesakura.util.DateUtil;

import org.junit.Test;

import android.net.Uri;

import java.io.File;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class GpxImporterTest extends AppUnitTestCase {

    @Test
    public void AACR2015のテストデータをインストールする() throws Exception {
        GpxImporter importer = new GpxImporter(getContext(), new File("../sdk/src/test/assets/gpx/sample-aacr2015.gpx").getAbsoluteFile());
        importer.getParser().setDateOption(GpxParser.DateOption.AddTimeZone);
        importer.install(null);


        assertEquals(DateUtil.getYear(importer.getImportStartDate(), TimeZone.getDefault()), 2015);
        assertEquals(DateUtil.getMonth(importer.getImportStartDate(), TimeZone.getDefault()), 5);
        assertEquals(DateUtil.getDay(importer.getImportStartDate(), TimeZone.getDefault()), 24);
        assertEquals(DateUtil.getHour(importer.getImportStartDate(), TimeZone.getDefault()), 5);

        assertEquals(DateUtil.getYear(importer.getImportEndDate(), TimeZone.getDefault()), 2015);
        assertEquals(DateUtil.getMonth(importer.getImportEndDate(), TimeZone.getDefault()), 5);
        assertEquals(DateUtil.getDay(importer.getImportEndDate(), TimeZone.getDefault()), 24);
        assertEquals(DateUtil.getHour(importer.getImportEndDate(), TimeZone.getDefault()), 16);
    }
}
