package com.eaglesakura.andriders.trigger.launcher;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandSetting;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.IconItemAdapter;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.material.widget.support.SupportRecyclerView;
import com.eaglesakura.util.CollectionUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v7.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;


@FragmentLayout(R.layout.trigger_launcher)
public class LauncherSelectFragmentMain extends AppFragment {

    @Bind(R.id.Content_List)
    SupportRecyclerView mItems;

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
        mItems.getRecyclerView().setLayoutManager(new GridLayoutManager(getContext(), 3));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLaunchers();
    }

    @UiThread
    void loadLaunchers() {
        asyncUI((BackgroundTask<List<ResolveInfo>> task) -> {
            return listLauncherApplications(getContext());
        }).completed((result, task) -> {
            mItems.setAdapter(mAdapter, true);
            mAdapter.getCollection()
                    .addAll(CollectionUtil.asOtherList(result, it -> new IconItemAdapter.LauncherItem(getContext(), it)));
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
    }

    IconItemAdapter<IconItemAdapter.LauncherItem> mAdapter = new IconItemAdapter<IconItemAdapter.LauncherItem>(mLifecycleDelegate ) {
        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        protected void onItemSelected(int position, LauncherItem item) {
            onSelected(item.getInfo());
        }
    };

    /**
     * アイテム選択されたら前の画面に戻る
     */
    @UiThread
    void onSelected(ResolveInfo info) {
        CommandSetting.Builder
                .makeActivity(getContext(), new ComponentName(info.activityInfo.packageName, info.activityInfo.name))
                .setIcon(((BitmapDrawable) info.loadIcon(getContext().getPackageManager())).getBitmap())
                .finish(getActivity());
    }

    /**
     * Launcher属性のアプリをすべて取得する
     */
    public static List<ResolveInfo> listLauncherApplications(Context context) {
        PackageManager pm = context.getPackageManager();
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
