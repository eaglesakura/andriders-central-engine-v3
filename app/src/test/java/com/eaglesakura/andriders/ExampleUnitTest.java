package com.eaglesakura.andriders;

import com.eaglesakura.util.LogUtil;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExampleUnitTest extends AceJUnitTester {
    @Test
    public void helloContextTest() throws Exception {
        LogUtil.log("Hello JUnit Test!!");

        assertNotNull(mContext);
        assertNotNull(mContext instanceof AceApplication);
        assertNotNull(mContext.getString(R.string.Common_File_Load));
        assertTrue(BuildConfig.DEBUG == BuildConfig.APPLICATION_ID.endsWith(".debug"));
    }
}