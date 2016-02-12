package com.eaglesakura.andriders;

import org.junit.Test;
import org.robolectric.shadows.ShadowLog;

import android.util.Log;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExampleUnitTest extends AceJUnitTester {
    @Test
    public void helloContextTest() throws Exception {
        assertNotNull(mContext);
        assertNotNull(mContext instanceof AceApplication);
        assertNotNull(mContext.getString(R.string.Common_File_Load));
        assertTrue(BuildConfig.DEBUG);

        ShadowLog.stream = System.out;
        Log.i("TEST", "Hello JUnit Test!!");
    }
}