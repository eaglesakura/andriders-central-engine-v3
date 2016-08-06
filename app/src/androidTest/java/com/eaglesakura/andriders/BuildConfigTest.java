package com.eaglesakura.andriders;

import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.android.framework.FrameworkCentral;

import org.junit.Test;

public class BuildConfigTest extends DeviceTestCase<AceApplication> {

    @Test
    public void 正しいApplicationContextを得ることができる() {
        assertTrue(getApplication() instanceof AceApplication);
        assertTrue(FrameworkCentral.getApplication() instanceof AceApplication);
    }

}
