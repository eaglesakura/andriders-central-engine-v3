package com.eaglesakura.andriders.basicui.command;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandSetting;
import com.eaglesakura.andriders.databinding.CardLauncherBinding;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.ui.support.SupportActivity;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.material.widget.support.SupportRecyclerView;
import com.eaglesakura.util.CollectionUtil;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class LauncherSelectActivity extends SupportActivity {

    @Bind(R.id.LauncherSelect_List_Root)
    SupportRecyclerView mSupportRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_select);

        // Toolbarをバインド
        {
            Toolbar toolbar = findViewById(Toolbar.class, R.id.EsMaterial_Toolbar);
            setSupportActionBar(toolbar);
            setTitle("起動するアプリを選択");
        }

        loadLaunchers();
    }

    @UiThread
    void loadLaunchers() {
        asyncUI((RxTask<List<ResolveInfo>> task) -> {
            return listLauncherApplications();
        }).completed((result, task) -> {
            mAdapter.getCollection().addAllAnimated(result);
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
        mSupportRecyclerView.setAdapter(mAdapter, true);
    }

    CardAdapter<ResolveInfo> mAdapter = new CardAdapter<ResolveInfo>() {
        @Override
        protected View onCreateCard(ViewGroup parent, int viewType) {
            return CardLauncherBinding.inflate(getLayoutInflater(), null, false).getRoot();
        }

        @Override
        protected void onBindCard(CardBind<ResolveInfo> bind, int position) {
            ResolveInfo item = bind.getItem();
            CardLauncherBinding binding = DataBindingUtil.getBinding(bind.getCard());
            binding.LauncherSelectItem.setOnClickListener(it -> {
                onItemSelected(item);
            });
            binding.setItem(new CardBinding() {
                @Override
                public Drawable getIcon() {
                    return item.loadIcon(getPackageManager());
                }

                @Override
                public String getTitle() {
                    return item.loadLabel(getPackageManager()).toString();
                }
            });
        }
    };

    /**
     * アイテム選択されたら前の画面に戻る
     */
    @UiThread
    Intent onItemSelected(ResolveInfo info) {
        return CommandSetting.Builder
                .makeActivity(this, new ComponentName(info.activityInfo.packageName, info.activityInfo.name))
                .setIcon(((BitmapDrawable) info.loadIcon(getPackageManager())).getBitmap())
                .finish(this);
    }

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

    public interface CardBinding {
        Drawable getIcon();

        String getTitle();
    }
}
