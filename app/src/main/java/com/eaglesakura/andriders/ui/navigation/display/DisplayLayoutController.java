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
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.DateUtil;
import com.eaglesakura.util.StringUtil;

import org.greenrobot.greendao.annotation.NotNull;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.eaglesakura.sloth.util.AppSupportUtil.assertNotCanceled;

/**
 * レイアウトデータ管理
 */
public class DisplayLayoutController {
    private Context mContext;

    /**
     * キャッシュされたレイアウト情報
     */
    private Map<String, DisplayLayoutGroup> mLayouts = new HashMap<>();

    /**
     * 表示用UIを持ったプラグイン一覧
     */
    private DataCollection<CentralPlugin> mDisplayPlugins;

    @Inject(AppManagerProvider.class)
    private DisplayLayoutManager mDisplayLayoutManager;

    /**
     * ソート対象のアプリ
     */
    @Nullable
    private DataCollection<DisplayLayoutApplication> mApplications;

    /**
     * デフォルト構成のアプリ
     * package名はnull
     */
    @NonNull
    private DisplayLayoutApplication mDefaultApplication;


    /**
     * 選択されているアプリ
     */
    @NotNull
    private DisplayLayoutApplicationStream mSelectedAppStream = new DisplayLayoutApplicationStream();

    /**
     * ACE上でレイアウト設定されているアプリにつけるバッジアイコン
     */
    @NonNull
    private Drawable mBadgeIcon;

    public DisplayLayoutController(Context context) {
        mContext = context;
        mBadgeIcon = DrawableUtil.getVectorDrawable(context, R.drawable.ic_cycle_computer, R.color.App_Icon_Grey);
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

        mDefaultApplication = new DisplayLayoutApplication(mContext, null, mBadgeIcon);
        mSelectedAppStream.onUpdate(mDefaultApplication);
    }

    /**
     * 切替対象となるアプリ一覧をロードする
     *
     * キャッシュがある場合はキャッシュを利用し、それ以外の場合はロードを実行する。
     */
    public void loadTargetApplications(CancelCallback cancelCallback) throws TaskCanceledException {
        if (mSelectedAppStream.getValue() == null) {
            throw new IllegalStateException("load() not called!");
        }

        if (mApplications != null && !mApplications.isEmpty()) {
            // キャッシュがある
            return;
        }

        assertNotCanceled(cancelCallback);

        List<DisplayLayoutApplication> result = new ArrayList<>();
        result.add(mSelectedAppStream.getValue());   // デフォルト構成用

        Set<String> existPackageNames = CollectionUtil.asOtherSet(PackageUtil.listLauncherApplications(mContext), it -> it.activityInfo.packageName);
        for (ApplicationInfo info : PackageUtil.listInstallApplications(mContext)) {
            if (existPackageNames.contains(info.packageName)) {
                AppLog.system("Load TargetLauncher package[%s]", info.packageName);
                result.add(new DisplayLayoutApplication(mContext, info, mBadgeIcon));
            }

            assertNotCanceled(cancelCallback);
        }

        mApplications = new DataCollection<>(result);
        mApplications.setComparator((a, b) -> {
            DisplayLayoutGroup aGroup = mLayouts.get(a.getPackageName());
            DisplayLayoutGroup bGroup = mLayouts.get(b.getPackageName());

            if (aGroup == null && bGroup == null) {
                return a.getPackageName().compareTo(b.getPackageName());
            } else if (aGroup != null && bGroup == null) {
                return -1;
            } else if (aGroup == null && bGroup != null) {
                return 1;
            }

            Date aDate = aGroup.getUpdateDate();
            Date bDate = bGroup.getUpdateDate();
            return Long.compare(
                    aDate != null ? aDate.getTime() : DateUtil.currentTimeMillis(),
                    bDate != null ? bDate.getTime() : DateUtil.currentTimeMillis()
            );
        });
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
     * 編集対象のアプリをハンドリングするSteam
     */
    @NonNull
    public DisplayLayoutApplicationStream getSelectedAppStream() {
        return mSelectedAppStream;
    }

    /**
     * 現在選択されているアプリのレイアウトを更新する
     */
    public void setLayout(DisplayLayout layout) {
        DisplayLayoutApplication app = mSelectedAppStream.getValueOrDefault(mDefaultApplication);
        DisplayLayoutGroup layoutGroup = getLayoutGroup(app.getPackageName());
        layoutGroup.insertOrReplace(layout);
    }

    /**
     * 現在の状況に応じてソートされたアプリ一覧を取得する
     */
    public List<DisplayLayoutApplication> listSortedApplications() {
        if (mApplications == null) {
            return new ArrayList<>();
        }

        return mApplications.list();
    }

    /**
     * アプリの表示優先度を設定するキャッシュ値
     */
    private static class ApplicationSortCache {
        /**
         * 操作された最終日付
         */
        private Date mUpdatedDate;

        /**
         * 操作対象のアプリID
         */
        private String mPackageName;

        public ApplicationSortCache(Date updatedDate, String packageName) {
            mUpdatedDate = updatedDate;
            mPackageName = packageName;
        }

        /**
         * 昇順ソート
         */
        public static final Comparator<ApplicationSortCache> COMPARATOR_ASC = (a, b) -> {
            if (a.mUpdatedDate != null && b.mUpdatedDate != null) {
                // 値が大きい方を優先させる
                return Long.compare(b.mUpdatedDate.getTime(), a.mUpdatedDate.getTime());
            } else if (a.mUpdatedDate != null) {
                return -1;
            } else if (b.mUpdatedDate != null) {
                return 1;
            } else {
                return StringUtil.compareString(a.mPackageName, b.mPackageName);
            }
        };
    }
}
