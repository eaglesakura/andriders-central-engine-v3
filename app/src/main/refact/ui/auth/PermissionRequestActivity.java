package com.eaglesakura.andriders.ui.auth;

import com.eaglesakura.android.framework.ui.support.SupportActivity;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.util.CollectionUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class PermissionRequestActivity extends SupportActivity {

    static final String EXTRA_REQUEST_PERMISSIONS = "EXTRA_REQUEST_PERMISSIONS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<String> permissions = getIntent().getStringArrayListExtra(EXTRA_REQUEST_PERMISSIONS);
        if (CollectionUtil.isEmpty(permissions)) {
            finish();
            return;
        }

        if (!requestRuntimePermissions(CollectionUtil.asArray(permissions, new String[permissions.size()]))) {
            // RuntimePermissionが起動しなければ何もしない
            finish();
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        finish();
    }

    /**
     * 指定したパーミッションのIntent取得を開始する
     */
    public static Intent createIntent(Context context, PermissionUtil.PermissionType[] permissions) {
        Intent intent = new Intent(context, PermissionRequestActivity.class);
        ArrayList<String> extra = new ArrayList<>();
        for (int i = 0; i < permissions.length; ++i) {
            for (String perm : permissions[i].getPermissions()) {
                extra.add(perm);
            }
        }
        intent.putStringArrayListExtra(EXTRA_REQUEST_PERMISSIONS, extra);
        return intent;
    }
}
