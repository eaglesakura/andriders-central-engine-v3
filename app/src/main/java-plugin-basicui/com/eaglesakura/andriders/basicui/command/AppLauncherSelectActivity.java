package com.eaglesakura.andriders.basicui.command;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.android.framework.ui.support.SupportActivity;
import com.eaglesakura.util.CollectionUtil;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

public class AppLauncherSelectActivity extends SupportActivity {


    /**
     * Launcher属性のアプリをすべて取得する
     */
    public List<ResolveInfo> listLauncherApplications() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> result = new ArrayList<>();
        try {
            CollectionUtil.each(pm.queryIntentActivities(intent, 0), it -> {
                // MEMO: 後でフィルタリングができるようにしておく

                result.add(it);
            });
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
