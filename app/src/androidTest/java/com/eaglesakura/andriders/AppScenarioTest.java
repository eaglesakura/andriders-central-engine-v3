package com.eaglesakura.andriders;

import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.sloth.app.SlothActivity;
import com.eaglesakura.util.Util;

import org.junit.Rule;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

/**
 * シナリオテスト用
 */
public abstract class AppScenarioTest<ActivityClass extends SlothActivity> extends AppDeviceTestCase {

    @Rule
    public final ActivityTestRule<ActivityClass> mRule;

    public AppScenarioTest(Class<ActivityClass> clazz) {
        mRule = new ActivityTestRule<>(clazz);
    }

    @Override
    public void onSetup() {
        super.onSetup();
        autoBootActivity();
    }

    protected void autoBootActivity() {
        Garnet.override(AppStorageProvider.class, AppStorageProvider.class);
        mRule.launchActivity(new Intent());
        Util.sleep(1000);
    }
}
