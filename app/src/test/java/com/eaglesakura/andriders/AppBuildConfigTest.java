package com.eaglesakura.andriders;

import com.eaglesakura.thread.Holder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppBuildConfigTest extends AppUnitTestCase {
    @Test
    public void 正しいClassがロードできている() throws Exception {
        assertNotNull(getContext());
        assertNotNull(getContext() instanceof AceApplication);
        assertNotNull(getContext().getString(R.string.Common_File_Load));
        assertTrue(BuildConfig.DEBUG == BuildConfig.VERSION_NAME.endsWith(".debug"));
    }

    @Test
    public void ラムダ式を呼び出す() throws Exception {
        Holder<Boolean> holder = new Holder<>();
        holder.set(Boolean.FALSE);

        Runnable call = () -> {
            holder.set(Boolean.TRUE);
        };

        call.run();

        assertEquals(holder.get(), Boolean.TRUE);
    }
}