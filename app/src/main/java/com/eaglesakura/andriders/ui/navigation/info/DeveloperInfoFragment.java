package com.eaglesakura.andriders.ui.navigation.info;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.data.common.Link;
import com.eaglesakura.andriders.data.res.AppImageLoader;
import com.eaglesakura.andriders.databinding.SystemInfoDeveloperRowBinding;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.material.widget.support.SupportCancelCallbackBuilder;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
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

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        mImageLoader = findInterfaceOrThrow(AppImageLoader.Holder.class).getImageLoader();
        mAppSettings.getConfig().getAboutInfo().listDeveloperLinks().safeEach(developer -> {
            addDeveloperView(developer);
        });
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
        async(ExecuteTarget.Network, CallbackTime.Foreground, (BackgroundTask<Drawable> task) -> {
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
