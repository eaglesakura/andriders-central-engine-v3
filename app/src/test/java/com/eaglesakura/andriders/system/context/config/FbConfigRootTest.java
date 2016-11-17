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
            assertNotNull(root.profile);
            validate(root.profile.wheel).sizeFrom(1).allNotNull().each(item -> {
                assertNotEmpty(item.title);
                validate(item.length).from(1000).to(3000);
            });
        }
    }
}