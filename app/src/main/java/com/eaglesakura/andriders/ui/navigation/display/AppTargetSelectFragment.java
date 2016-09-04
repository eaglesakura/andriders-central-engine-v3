package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.basicui.command.LauncherSelectActivity;
import com.eaglesakura.andriders.dao.display.DbDisplayTarget;
import com.eaglesakura.andriders.databinding.CardLauncherBinding;
import com.eaglesakura.andriders.display.data.DataLayoutManager;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.PackageUtil;
import com.eaglesakura.android.util.ResourceUtil;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.util.CollectionUtil;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.UiThread;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;
import java.util.Set;

/**
 * 表示対象のアプリを選択するFragment
 */
public class AppTargetSelectFragment extends AppBaseFragment {

    @BundleState
    String mCurrentPackageName = BuildConfig.APPLICATION_ID;

    Callback mCallback;

    public AppTargetSelectFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_setting_display_appselect);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = getParentOrThrow(Callback.class);
    }

    /**
     * 削除ボタンが押されたら、データを削除してデフォルトに切り替える
     */
    @OnClick(R.id.Setting_CycleComputer_TargetApplication_Delete)
    void clickDeleteButton() {
        mCallback.onRequestDeleteLayout(this, mCurrentPackageName);

        // デフォルトレイアウトを読み込む
        AppInfo info = new AppInfo(
                ((BitmapDrawable) ResourceUtil.drawable(getContext(), R.mipmap.ic_launcher)).getBitmap(),
                getString(R.string.Setting_CycleComputer_Target_Default),
                BuildConfig.DEFAULT_PACKAGE_NAME
        );
        onSelectedLauncher(info);
    }

    /**
     * ランチャーボタンが押されたらBottomSheetでランチャーを選択させる
     */
    @OnClick(R.id.Setting_CycleComputer_TargetApplication_Root)
    void clickTargetApplicationChange() {
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        asyncUI((BackgroundTask<ViewGroup> task) -> {
            try (ProgressToken token = pushProgress(R.string.Common_File_Load)) {
                return buildAppSelector(task, dialog);
            }
        }).completed((result, task) -> {
            dialog.setContentView(result);
            addAutoDismiss(dialog).show();
        }).failed((error, task) -> {
            AppLog.printStackTrace(error);
        }).start();
    }

    /**
     * ロード済みのアプリ一覧を取得する
     */
    DataCollection<AppInfoCache> listInstalledApplications() throws Throwable {
        DataLayoutManager layoutManager = new DataLayoutManager(getContext());
        DataCollection<DbDisplayTarget> customizedDisplays = layoutManager.listCustomizedDisplays();

        Map<String, DbDisplayTarget> displayTargetMap = customizedDisplays.asMap(it -> true, it -> it.getTargetPackage());
        Set<String> customizedPackageNames = displayTargetMap.keySet();
        PackageManager packageManager = getContext().getPackageManager();
        DataCollection<AppInfoCache> installedApplications =
                new DataCollection<>(CollectionUtil.asOtherList(PackageUtil.listInstallApplications(getActivity()), it -> {
                    return new AppInfoCache(it, customizedPackageNames.contains(it.packageName));
                }));

        // アプリ一覧をソートする
        installedApplications.setComparator((a, b) -> {
            String aPackageName = a.info.packageName;
            String bPackageName = b.info.packageName;
            if (aPackageName.equals(BuildConfig.APPLICATION_ID)) {
                // ACEは最優先
                return -1;
            } else if (bPackageName.equals(BuildConfig.APPLICATION_ID)) {
                return 1;
            }

            DbDisplayTarget aDisplayTarget = displayTargetMap.get(aPackageName);
            DbDisplayTarget bDisplayTarget = displayTargetMap.get(bPackageName);

            if (aDisplayTarget != null && bDisplayTarget != null) {
                // 両方設定済みなので、その中でラベル優先
                return a.info.loadLabel(packageManager).toString().compareTo(b.info.loadLabel(packageManager).toString());
            } else if (aDisplayTarget != null) {
                return -1;
            } else if (bDisplayTarget != null) {
                return 1;
            } else {
                // 両方未インストール、名前優先
                return a.info.loadLabel(packageManager).toString().compareTo(b.info.loadLabel(packageManager).toString());
            }
        });

        return installedApplications;
    }

    /**
     * アプリ選択UIを組み立てる
     */
    ViewGroup buildAppSelector(BackgroundTask task, Dialog dialog) throws Throwable {
        // アプリ一覧をロードする
        DataCollection<AppInfoCache> installedApplications = listInstalledApplications();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.bottomsheet_root, null);
        // 列挙して扱う
        installedApplications.sortEach(it -> {
            task.throwIfCanceled();

            // UIスレッドに処理を移譲する
            UIHandler.await(() -> {
                CardLauncherBinding binding = CardLauncherBinding.inflate(inflater, (ViewGroup) layout.findViewById(R.id.Widget_BottomSheet_Root), true);
                binding.setItem(new LauncherSelectActivity.CardBinding() {
                    @Override
                    public Drawable getIcon() {
                        return it.info.loadIcon(getContext().getPackageManager());
                    }

                    @Override
                    public String getTitle() {
                        if (it.info.packageName.equals(BuildConfig.APPLICATION_ID)) {
                            return getString(R.string.Setting_CycleComputer_Target_Default);
                        }

                        return it.info.loadLabel(getContext().getPackageManager()).toString();
                    }
                });

                // ボタンを押されたら、コールバックしてダイアログを閉じる
                binding.LauncherSelectItem.setOnClickListener(view -> {
                    PackageManager packageManager = getContext().getPackageManager();
                    String title;
                    if (it.info.packageName.equals(BuildConfig.APPLICATION_ID)) {
                        title = getString(R.string.Setting_CycleComputer_Target_Default);
                    } else {
                        title = it.info.loadLabel(packageManager).toString();
                    }

                    AppInfo info = new AppInfo(
                            ((BitmapDrawable) it.info.loadIcon(packageManager)).getBitmap(),
                            title,
                            it.info.packageName
                    );
                    onSelectedLauncher(info);
                    dialog.dismiss();
                });

                return 0;
            });
        });
        return layout;
    }

    /**
     * アプリが選択されたら、UIを切り替える
     */
    @UiThread
    void onSelectedLauncher(AppInfo info) {
        mCurrentPackageName = info.getPackageName();
        mCallback.onApplicationSelected(this, info);

        new AQuery(getView())
                .id(R.id.Setting_CycleComputer_TargetApplication_Icon).image(info.getIcon())
                .id(R.id.Setting_CycleComputer_TargetApplication_Title).text(info.getTitle())
                .id(R.id.Setting_CycleComputer_TargetApplication_Delete).visibility(info.getPackageName().equals(BuildConfig.APPLICATION_ID) ? View.GONE : View.VISIBLE)
        ;
    }

    public interface Callback {
        /**
         * 表示対象のアプリが選択された
         */
        void onApplicationSelected(AppTargetSelectFragment fragment, AppInfo selected);

        /**
         * 削除がリクエストされた
         *
         * @param packageName 削除対象のパッケージ名
         */
        void onRequestDeleteLayout(AppTargetSelectFragment fragment, String packageName);
    }

    static class AppInfoCache {
        /**
         * アプリ情報
         */
        ApplicationInfo info;

        /**
         * 設定を作成済みであればtrue
         */
        boolean mSettingCreated;

        public AppInfoCache(ApplicationInfo info, boolean settingCreated) {
            this.info = info;
            mSettingCreated = settingCreated;
        }
    }

    public static class AppInfo {
        private final Bitmap icon;
        private final String title;
        private final String packageName;

        public AppInfo(Bitmap icon, String title, String packageName) {
            this.icon = icon;
            this.title = title;
            this.packageName = packageName;
        }

        public Bitmap getIcon() {
            return icon;
        }

        public String getTitle() {
            return title;
        }

        public String getPackageName() {
            return packageName;
        }
    }
}
