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
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.PackageUtil;
import com.eaglesakura.collection.DataCollection;

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

import java.util.Set;

/**
 * 表示対象のアプリを選択するFragment
 */
public class AppTargetSelectFragment extends AppBaseFragment {

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
     * アプリ選択UIを組み立てる
     */
    ViewGroup buildAppSelector(BackgroundTask task, BottomSheetDialog dialog) throws Throwable {
        DataLayoutManager layoutManager = new DataLayoutManager(getContext());
        DataCollection<DbDisplayTarget> customizedDisplays = layoutManager.listCustomizedDisplays();

        Set<String> customizedPackageNames = customizedDisplays.asMap(it -> true, it -> it.getTargetPackage()).keySet();
        PackageManager packageManager = getContext().getPackageManager();
        DataCollection<ApplicationInfo> installedApplications = new DataCollection<>(PackageUtil.listInstallApplications(getActivity()));

        LayoutInflater inflater = getActivity().getLayoutInflater();


        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.bottomsheet_root, null);

        // アプリ一覧をソートする
        installedApplications.setComparator((a, b) -> {
            String aPackageName = a.packageName;
            String bPackageName = b.packageName;
            if (aPackageName.equals(BuildConfig.APPLICATION_ID)) {
                // ACEは最優先
                return -1;
            } else if (bPackageName.equals(BuildConfig.APPLICATION_ID)) {
                return 1;
            }


            if (customizedPackageNames.contains(aPackageName) && customizedPackageNames.contains(bPackageName)) {
                // 両方インストール済みなので、名前優先
                return a.loadLabel(packageManager).toString().compareTo(b.loadLabel(packageManager).toString());
            } else if (!customizedPackageNames.contains(aPackageName) && !customizedPackageNames.contains(bPackageName)) {
                // 両方未インストール、名前優先
                return a.loadLabel(packageManager).toString().compareTo(b.loadLabel(packageManager).toString());
            } else if (customizedPackageNames.contains(aPackageName)) {
                // 片方インストール済み
                return -1;
            } else {
                return 1;
            }
        });

        // 列挙して扱う
        installedApplications.sortEach(it -> {
            task.throwIfCanceled();

            // UIスレッドに処理を移譲する
            UIHandler.await(() -> {
                CardLauncherBinding binding = CardLauncherBinding.inflate(inflater, layout, true);
                binding.setItem(new LauncherSelectActivity.CardBinding() {
                    @Override
                    public Drawable getIcon() {
                        return it.loadIcon(getContext().getPackageManager());
                    }

                    @Override
                    public String getTitle() {
                        return it.loadLabel(getContext().getPackageManager()).toString();
                    }
                });

                // ボタンを押されたら、コールバックしてダイアログを閉じる
                binding.LauncherSelectItem.setOnClickListener(view -> {
                    AppInfo info = new AppInfo(
                            ((BitmapDrawable) it.loadIcon(packageManager)).getBitmap(),
                            it.loadLabel(packageManager).toString(),
                            it.packageName
                    );
                    onSelectedLauncher(info);
                    dialog.dismiss();
                });

                return 0;
            });
        });
        return layout;
    }

    @UiThread
    void onSelectedLauncher(AppInfo info) {
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
