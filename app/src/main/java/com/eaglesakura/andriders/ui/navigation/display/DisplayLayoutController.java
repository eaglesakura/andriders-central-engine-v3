package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.data.display.DisplayLayoutManager;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.model.display.DisplayLayoutCollection;
import com.eaglesakura.andriders.model.display.DisplayLayoutGroup;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.CentralPluginCollection;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginDataManager;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.util.DrawableUtil;
import com.eaglesakura.android.util.PackageUtil;
import com.eaglesakura.cerberus.error.TaskCanceledException;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.sloth.data.DataBus;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.eaglesakura.sloth.util.AppSupportUtil.assertNotCanceled;

/**
 * レイアウトデータ管理
 */
public class DisplayLayoutController {
    Context mContext;

    /**
     * キャッシュされたレイアウト情報
     */
    Map<String, DisplayLayoutGroup> mLayouts = new HashMap<>();

    /**
     * 表示用UIを持ったプラグイン一覧
     */
    DataCollection<CentralPlugin> mDisplayPlugins;

    @Inject(AppManagerProvider.class)
    DisplayLayoutManager mDisplayLayoutManager;

    /**
     * ソート対象のアプリ
     */
    @Nullable
    DataCollection<DisplayLayoutApplication> mApplications;

    /**
     * デフォルト構成のアプリ
     * package名はnull
     */
    @NonNull
    DisplayLayoutApplication mDefaultApplication;

    /**
     * 選択されているアプリ
     */
    @NonNull
    DisplayLayoutApplication mSelectedApp;

    /**
     * ACE上でレイアウト設定されているアプリにつけるサブアイコン
     */
    @NonNull
    Drawable mSubIcon;

    public DisplayLayoutController(Context context) {
        mContext = context;
        mSubIcon = DrawableUtil.getVectorDrawable(context, R.drawable.ic_cycle_computer, R.color.App_Icon_Grey);
        Garnet.create(this)
                .depend(Context.class, context)
                .inject();
    }

    /**
     * 表示プラグインが1個以上有効化されている場合true
     */
    @Nullable
    public boolean hasDisplays() {
        return !mDisplayPlugins.isEmpty();
    }

    @Nullable
    public DisplayKey getDisplayKey(DisplayLayout layout) {
        // 一致するプラグインを検索する
        CentralPlugin find = mDisplayPlugins.find(plugin -> plugin.getId().equals(layout.getPluginId()));
        if (find == null) {
            return null;
        }

        // 一致する値のキーを検索する
        return find.listDisplayKeys().find(key -> key.getId().equals(layout.getValueId()));
    }

    /**
     * 管理グループを取得する
     *
     * @param packageName パッケージ名
     */
    public DisplayLayoutGroup getLayoutGroup(String packageName) {
        synchronized (mLayouts) {
            if (StringUtil.isEmpty(packageName)) {
                packageName = DisplayLayout.PACKAGE_NAME_DEFAULT;
            }

            DisplayLayoutGroup result = mLayouts.get(packageName);
            if (result == null) {
                result = new DisplayLayoutGroup(new ArrayList<>(), packageName);
                // デフォルト構成を生成する
                for (int h = 0; h < DisplayLayoutManager.MAX_HORIZONTAL_SLOTS; ++h) {
                    for (int v = 0; v < DisplayLayoutManager.MAX_VERTICAL_SLOTS; ++v) {
                        DisplayLayout layout = new DisplayLayout.Builder(DisplayLayout.getSlotId(h, v))
                                .application(packageName)
                                .build();
                        result.insertOrReplace(layout);
                    }
                }
                mLayouts.put(packageName, result);
            }
            return result;
        }
    }

    /**
     * ディスプレイ情報をすべてロードしてキャッシュ化する
     */
    public void load(CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        PluginDataManager pluginDataManager = Garnet.instance(AppManagerProvider.class, PluginDataManager.class);

        CentralPluginCollection plugins = pluginDataManager.listPlugins(PluginDataManager.PluginListingMode.Active, cancelCallback);
        try {
            plugins.connect(new CentralPlugin.ConnectOption(), cancelCallback);

            mDisplayPlugins = new DataCollection<>(plugins.listDisplayPlugins());
            for (CentralPlugin plugin : mDisplayPlugins.getSource()) {
                plugin.getName();   // 名前をロードしておく
                PluginInformation information = plugin.getInformation();
                AppLog.plugin("DisplayPlugin id[%s] category[%s]", information.getId(), information.getCategory());
            }
        } finally {
            plugins.disconnect();
        }

        // すべてのレイアウトを列挙し、グルーピングする
        DisplayLayoutCollection allLayout = mDisplayLayoutManager.list();
        for (DisplayLayout layout : allLayout.getSource()) {
            DisplayLayoutGroup group = getLayoutGroup(layout.getAppPackageName());
            group.insertOrReplace(layout);
        }

        mDefaultApplication = new DisplayLayoutApplication(mContext, null, mSubIcon);
        mSelectedApp = mDefaultApplication;
    }

    /**
     * 切替対象となるアプリ一覧をロードする
     *
     * キャッシュがある場合はキャッシュを利用し、それ以外の場合はロードを実行する。
     */
    public void loadTargetApplications(CancelCallback cancelCallback) throws TaskCanceledException {
        if (mSelectedApp == null) {
            throw new IllegalStateException("load() not called!");
        }

        if (mApplications != null && !mApplications.isEmpty()) {
            // キャッシュがある
            return;
        }

        assertNotCanceled(cancelCallback);

        List<DisplayLayoutApplication> result = new ArrayList<>();
        result.add(mSelectedApp);   // デフォルト構成用

        Set<String> existPackageNames = CollectionUtil.asOtherSet(PackageUtil.listLauncherApplications(mContext), it -> it.activityInfo.packageName);
        for (ApplicationInfo info : PackageUtil.listInstallApplications(mContext)) {
            if (existPackageNames.contains(info.packageName)) {
                AppLog.system("Load TargetLauncher package[%s]", info.packageName);
                result.add(new DisplayLayoutApplication(mContext, info, mSubIcon));
            }

            assertNotCanceled(cancelCallback);
        }

        mApplications = new DataCollection<>(result);
        mApplications.setComparator(DisplayLayoutApplication.COMPARATOR_ASC);
    }

    /**
     * データ保存を行う
     */
    public void commit() throws AppException {
        synchronized (mLayouts) {
            List<DisplayLayout> buffer = new ArrayList<>(mLayouts.size() * DisplayLayoutManager.MAX_VERTICAL_SLOTS * DisplayLayoutManager.MAX_HORIZONTAL_SLOTS);
            for (DisplayLayoutGroup group : mLayouts.values()) {
                buffer.addAll(group.getSource());
            }
            mDisplayLayoutManager.update(buffer);
        }
    }

    /**
     * 指定したアプリパッケージ構成を削除する
     *
     * @param packageName アプリパッケージ名
     */
    public void remove(@NonNull String packageName) throws AppException {
        if (StringUtil.isEmpty(packageName)) {
            throw new NullPointerException("packageName == null");
        }
        synchronized (mLayouts) {
            mDisplayLayoutManager.removeAll(packageName);   // DB削除
            mLayouts.remove(packageName);   // メモリキャッシュ削除
        }
    }

    /**
     * 設定されていない場合のグローバル設定を取得する
     */
    @NonNull
    public DisplayLayoutApplication getDefaultApplication() {
        return mDefaultApplication;
    }

    /**
     * 関連するプラグインを列挙する
     */
    @NonNull
    public DataCollection<CentralPlugin> listPlugins() {
        return new DataCollection<>(mDisplayPlugins.getSource());
    }

    /**
     * 現在選択されているアプリを取得する
     */
    @NonNull
    public DisplayLayoutApplication getSelectedApp() {
        return mSelectedApp;
    }

    /**
     * 現在選択されているアプリのレイアウトを更新する
     */
    public void setLayout(DisplayLayout layout) {
        DisplayLayoutGroup layoutGroup = getLayoutGroup(getSelectedApp().getPackageName());
        layoutGroup.insertOrReplace(layout);
    }

    /**
     * 現在の状況に応じてソートされたアプリ一覧を取得する
     */
    public List<DisplayLayoutApplication> listSortedApplications() {
        for (DisplayLayoutApplication app : mApplications.getSource()) {
            app.mUpdatedDate = null;
            DisplayLayoutGroup group = mLayouts.get(app.getPackageName());
            if (group != null) {
                app.mUpdatedDate = group.getUpdateDate();
            }
        }

        return mApplications.list();
    }

    public static class Bus extends DataBus<DisplayLayoutController> {

        public Bus(@Nullable DisplayLayoutController data) {
            super(data);
        }

        public void onSelected(DisplayLayoutApplication app) {
            getData().mSelectedApp = app;
            modified();
        }
    }
}
