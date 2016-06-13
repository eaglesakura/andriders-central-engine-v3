package com.eaglesakura.andriders.db.plugin;

import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.dao.plugin.DaoMaster;
import com.eaglesakura.andriders.dao.plugin.DaoSession;
import com.eaglesakura.andriders.dao.plugin.DbActivePlugin;
import com.eaglesakura.andriders.dao.plugin.DbActivePluginDao;
import com.eaglesakura.android.db.DaoDatabase;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

/**
 * 有効なプラグイン等を管理する
 */
public class PluginDatabase extends DaoDatabase<DaoSession> {
    private static final int SUPPORTED_DATABASE_VERSION = 0x01;

    public PluginDatabase(Context context) {
        super(context, DaoMaster.class);
    }

    static String getPluginClassName(String packageName, String className) {
        return StringUtil.format("%s@%s", packageName, className);
    }

    public void active(PluginInformation pluginInfo, ResolveInfo appInfo) {
        DbActivePlugin dbActivePlugin = new DbActivePlugin();
        dbActivePlugin.setUniqueId(getPluginClassName(appInfo.serviceInfo.packageName, appInfo.serviceInfo.name));
        dbActivePlugin.setCategory(pluginInfo.getCategory().getName());
        dbActivePlugin.setPackageName(appInfo.serviceInfo.packageName);
        dbActivePlugin.setClassName(appInfo.serviceInfo.name);

        session.getDbActivePluginDao().insertOrReplace(dbActivePlugin);
    }

    /**
     * 指定した内蔵クラスを強制的にアクティブにする
     */
    public void active(Class clazz, Category category) {
        DbActivePlugin dbActivePlugin = new DbActivePlugin();
        dbActivePlugin.setUniqueId(getPluginClassName(context.getPackageName(), clazz.getName()));
        dbActivePlugin.setCategory(category.getName());
        dbActivePlugin.setPackageName(context.getPackageName());
        dbActivePlugin.setClassName(clazz.getName());

        session.getDbActivePluginDao().insertOrReplace(dbActivePlugin);

    }

    /**
     * 有効状態から削除する
     */
    public void remove(ResolveInfo appInfo) {
        session.getDbActivePluginDao().deleteByKey(getPluginClassName(appInfo.serviceInfo.packageName, appInfo.serviceInfo.name));
    }

    /**
     * 指定されたプラグインがActiveであればtrue
     */
    public boolean isActive(ResolveInfo appInfo) {
        return session.getDbActivePluginDao().queryBuilder()
                .where(DbActivePluginDao.Properties.PackageName.eq(appInfo.serviceInfo.packageName), DbActivePluginDao.Properties.ClassName.eq(appInfo.serviceInfo.name))
                .count() == 1;
    }

    /**
     * 指定したカテゴリのプラグインを全て無効化する
     *
     * @param categoryName カテゴリ名
     */
    public void disableCategoryPlugins(@NonNull String categoryName) {
        session.getDbActivePluginDao().queryBuilder()
                .where(DbActivePluginDao.Properties.Category.eq(categoryName))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    /**
     * 全てのプラグインを列挙する
     */
    public PluginCollection list() {
        return new PluginCollection(
                CollectionUtil.asOtherList(
                        session.getDbActivePluginDao().loadAll(),
                        it -> new ActivePlugin(it)
                )
        );
    }

    @Override
    protected SQLiteOpenHelper createHelper() {
        return new SQLiteOpenHelper(context, "plugins.db", null, SUPPORTED_DATABASE_VERSION) {

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                DaoMaster.createAllTables(db, false);
            }
        };
    }
}
