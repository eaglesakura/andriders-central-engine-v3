package com.eaglesakura.andriders.trigger.launcher;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandSetting;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.IconItemAdapter;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.util.CollectionUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


@FragmentLayout(R.layout.trigger_launcher)
public class LauncherSelectFragmentMain extends AppFragment {

    @Bind(R.id.Content_List)
    RecyclerView mItems;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mItems.setLayoutManager(new GridLayoutManager(getContext(), 3));
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLaunchers();
    }

    @UiThread
    void loadLaunchers() {
        asyncQueue((BackgroundTask<List<ResolveInfo>> task) -> {
            return listLauncherApplications(getContext());
        }).completed((result, task) -> {
            mItems.setAdapter(mAdapter);
            mAdapter.getCollection()
                    .addAll(CollectionUtil.asOtherList(result, it -> new IconItemAdapter.LauncherItem(getContext(), it)));
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
    }

    IconItemAdapter<IconItemAdapter.LauncherItem> mAdapter = new IconItemAdapter<IconItemAdapter.LauncherItem>(getLifecycle()) {
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
