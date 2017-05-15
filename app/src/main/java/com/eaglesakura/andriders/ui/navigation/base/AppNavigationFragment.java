package com.eaglesakura.andriders.ui.navigation.base;

import com.eaglesakura.android.oari.ActivityResult;

import android.content.Intent;

/**
 * アプリのメイン画面構築用Fragment
 */
public class AppNavigationFragment extends AppFragment {

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ActivityResult.invokeRecursive(this, requestCode, resultCode, data);
    }
}
