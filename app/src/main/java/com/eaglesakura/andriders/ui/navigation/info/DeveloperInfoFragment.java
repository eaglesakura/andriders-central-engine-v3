package com.eaglesakura.andriders.ui.navigation.info;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.data.common.Link;
import com.eaglesakura.andriders.data.res.AppImageLoader;
import com.eaglesakura.andriders.databinding.SystemInfoDeveloperRowBinding;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.util.FragmentUtil;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.TimeUnit;

/**
 * アプリのビルド時情報の表示を行う
 */
@FragmentLayout(R.layout.system_info_developer)
public class DeveloperInfoFragment extends AppFragment {

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    @Bind(R.id.Content_List_Root)
    ViewGroup mDeveloperLinks;

    @NonNull
    AppImageLoader mImageLoader;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mImageLoader = FragmentUtil.findInterface(this, getContext(), AppImageLoader.Holder.class).getImageLoader();
        mAppSettings.getConfig().getAboutInfo().listDeveloperLinks().safeEach(developer -> {
            addDeveloperView(developer);
        });
        return view;
    }

    /**
     * 開発者情報を追加する
     */
    void addDeveloperView(Link developer) {
        SystemInfoDeveloperRowBinding binding = SystemInfoDeveloperRowBinding.inflate(LayoutInflater.from(getContext()), mDeveloperLinks, false);
        binding.setItem(new DeveloperItem() {
            @Override
            public String getName() {
                return developer.getTitle();
            }

            @Override
            public Drawable getIcon() {
                return null;
            }
        });
        binding.Button.setOnClickListener(view -> onClickDeveloper(developer));
        getFragmentLifecycle().async(ExecuteTarget.Network, CallbackTime.Foreground, (BackgroundTask<Drawable> task) -> {
            SupportCancelCallbackBuilder.CancelChecker checker = SupportCancelCallbackBuilder.from(task).andTimeout(1000 * 60, TimeUnit.MILLISECONDS).build();
            return mImageLoader.newImage(developer.getIconUri(), false)
                    .keepAspectResize(256, 256)
                    .errorImage(R.mipmap.ic_launcher, false)
                    .await(checker);
        }).completed((result, task) -> {
            binding.setItem(new DeveloperItem() {
                @Override
                public String getName() {
                    return developer.getTitle();
                }

                @Override
                public Drawable getIcon() {
                    return result;
                }
            });
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
        mDeveloperLinks.addView(binding.getRoot());
    }

    /**
     * 開発者をクリックしたら該当リンクへ飛ぶ
     */
    @UiThread
    void onClickDeveloper(Link developer) {
        Intent intent = new Intent(Intent.ACTION_VIEW, developer.getLinkUri());
        startActivity(intent);
    }

    public interface DeveloperItem {
        String getName();

        Drawable getIcon();
    }
}
