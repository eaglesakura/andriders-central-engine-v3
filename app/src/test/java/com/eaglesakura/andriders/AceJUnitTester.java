package com.eaglesakura.andriders;

import com.eaglesakura.util.LogUtil;

import org.junit.Before;
import org.junit.runner.RunWith;

import org.hamcrest.core.Is;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import android.content.Context;

import java.io.File;

@RunWith(AceJUnitTestRunner.class)
@Config(constants = BuildConfig.class, application = AceApplication.class, packageName = BuildConfig.DEFAULT_PACKAGE_NAME)
public abstract class AceJUnitTester {
    protected Context mContext;

    private void initializeLogger() {
        ShadowLog.stream = System.out;
        LogUtil.setOutput(true);
        LogUtil.setLogger(new LogUtil.Logger() {
            @Override
            public void i(String msg) {
                try {
                    StackTraceElement[] trace = new Exception().getStackTrace();
                    StackTraceElement elem = trace[Math.min(trace.length - 1, 3)];
                    System.out.println("I " + String.format("%s[%d] : %s", elem.getFileName(), elem.getLineNumber(), msg));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void d(String msg) {
                try {
                    StackTraceElement[] trace = new Exception().getStackTrace();
                    StackTraceElement elem = trace[Math.min(trace.length - 1, 3)];
                    System.out.println("D " + String.format("%s[%d] : %s", elem.getFileName(), elem.getLineNumber(), msg));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Before
    public void onSetup() {
        mContext = RuntimeEnvironment.application;
        initializeLogger();
    }


    protected File getTestAsset(String path) {
        return new File("src/test/assets/" + path).getAbsoluteFile();
    }

    public static org.hamcrest.Matcher<Boolean> isTrue() {
        return Is.is(true);
    }
}
