package com.eaglesakura.andriders.system.context.config;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.json.JSON;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;

public class FbConfigRootTest extends AppUnitTestCase {

    @Test
    public void リファレンスデータがパースできる() throws Throwable {
        try (InputStream is = new FileInputStream("../firebase/database/configs/v1.json")) {
            FbConfigRoot root = JSON.decode(is, FbConfigRoot.class);
            assertNotNull(root);
            validate(root.profile.wheel).sizeFrom(1).allNotNull().each(item -> {
                assertNotEmpty(item.title);
                validate(item.length).from(1000).to(3000);
            });
            validate(root.profile.googleFitPackage).notNull().check(item -> {
                assertNotEmpty(item.title);
                assertNotEmpty(item.packageName);
                assertNotEmpty(item.className);
            });
            validate(root.sensor.gps.accuracyMeter).allNotNull().notEmpty().each(accuracy -> {
                validate((int) accuracy).from(1);
            });
            assertNotNull(root.aboutInfo.developer);
            validate(root.aboutInfo.developer.link).allNotNull().notEmpty().each(link -> {
                assertNotEmpty(link.title);
                assertNotEmpty(link.linkUrl);
                assertNotEmpty(link.iconUrl);
            });
        }
    }
}