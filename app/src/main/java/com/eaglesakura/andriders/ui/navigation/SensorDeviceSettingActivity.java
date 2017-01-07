package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.sensor.SensorDeviceSettingFragmentMain;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * センサー・周辺デバイスの設定画面
 */
public class SensorDeviceSettingActivity extends AppNavigationActivity {
    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new SensorDeviceSettingFragmentMain();
    }

    public static class Builder {
        Intent mIntent;

        public static Builder from(Context context) {
            Builder result = new Builder();
            result.mIntent = new Intent(context, SensorDeviceSettingActivity.class);
            return result;
        }

        public Intent build() {
            return mIntent;
        }
    }
}
