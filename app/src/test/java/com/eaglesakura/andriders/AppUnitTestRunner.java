package com.eaglesakura.andriders;

import org.junit.runners.model.InitializationError;

import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

/**
 * Custom Application Test runner
 *
 * https://github.com/robolectric/robolectric/issues/1430
 */
public class AppUnitTestRunner extends RobolectricTestRunner {
    public AppUnitTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        String manifest = String.format("build/intermediates/manifests/full/%1$s/%2$s/AndroidManifest.xml",
                BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE);
        String res = String.format("build/intermediates/res/merged/%1$s/%2$s",
                BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE);
        String asset = String.format("build/intermediates/assets/%1$s/%2$s",
                BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE);
        return new AndroidManifest(Fs.fileFromPath(manifest), Fs.fileFromPath(res),
                Fs.fileFromPath(asset)) {
            @Override
            public String getRClassName() throws Exception {
                return R.class.getName();
            }
        };
    }
}
