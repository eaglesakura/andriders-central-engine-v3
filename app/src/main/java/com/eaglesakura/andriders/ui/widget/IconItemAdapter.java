package com.eaglesakura.andriders.ui.widget;

import com.eaglesakura.andriders.databinding.WidgetIconitemRowBinding;
import com.eaglesakura.android.framework.delegate.lifecycle.LifecycleDelegate;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.material.widget.adapter.CardAdapter;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * アイコンを持つアイテムのセレクタ
 */
public abstract class IconItemAdapter<T extends IconItemAdapter.Item> extends CardAdapter<T> {
    final LifecycleDelegate mLifecycleDelegate;

    public IconItemAdapter(LifecycleDelegate delegate) {
        mLifecycleDelegate = delegate;
    }

    protected abstract Context getContext();

    @Override
    protected View onCreateCard(ViewGroup parent, int viewType) {
        return WidgetIconitemRowBinding.inflate(LayoutInflater.from(getContext()), null, false).getRoot();
    }

    @Override
    protected void onBindCard(CardBind<T> bind, int position) {
        WidgetIconitemRowBinding binding = bind.getBinding();
        binding.setItem(bind.getItem());
        binding.Item.setOnClickListener(view -> {
            onItemSelected(position, (T) binding.getItem());
        });
        binding.Icon.setVisibility(View.INVISIBLE);

        // 非同期でいアコンを処理する
        mLifecycleDelegate.async(ExecuteTarget.LocalParallel, CallbackTime.Foreground,  (BackgroundTask<Drawable> task) -> bind.getItem().getIcon())
                .completed((result, task) -> {
                    binding.Icon.setImageDrawable(result);
                    binding.Icon.setVisibility(View.VISIBLE);
                })
                .cancelSignal(task -> !bind.isBinded())
                .start();
    }

    /**
     * アイテム選択時
     *
     * @param position 選択されたインデックス
     * @param item     選択されたアイテム
     */
    @UiThread
    protected abstract void onItemSelected(int position, T item);

    public interface Item {
        /**
         * アイコン
         */
        Drawable getIcon();

        /**
         * 表示タイトル
         */
        String getTitle();
    }

    public static class LauncherItem implements Item {

        @NonNull
        final Context mContext;

        @NonNull
        final ResolveInfo mInfo;

        public LauncherItem(@NonNull Context context, @NonNull ResolveInfo info) {
            mContext = context;
            mInfo = info;
        }

        @Override
        public Drawable getIcon() {
            return mInfo.loadIcon(mContext.getPackageManager());
        }

        @Override
        public String getTitle() {
            return mInfo.loadLabel(mContext.getPackageManager()).toString();
        }

        @NonNull
        public ResolveInfo getInfo() {
            return mInfo;
        }
    }
}
