package com.eaglesakura.andriders.data.db;

import com.google.android.gms.fitness.data.BleDevice;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.dao.central.DaoMaster;
import com.eaglesakura.andriders.dao.central.DaoSession;
import com.eaglesakura.andriders.dao.central.DbActivePlugin;
import com.eaglesakura.andriders.dao.central.DbActivePluginDao;
import com.eaglesakura.andriders.dao.central.DbBleFitnessDevice;
import com.eaglesakura.andriders.dao.central.DbBleFitnessDeviceDao;
import com.eaglesakura.andriders.dao.central.DbCommand;
import com.eaglesakura.andriders.dao.central.DbCommandDao;
import com.eaglesakura.andriders.dao.central.DbDisplayLayout;
import com.eaglesakura.andriders.dao.central.DbDisplayLayoutDao;
import com.eaglesakura.andriders.dao.central.DbDisplayTarget;
import com.eaglesakura.andriders.model.ble.FitnessDeviceType;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.plugin.ActivePlugin;
import com.eaglesakura.andriders.model.plugin.ActivePluginCollection;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.andriders.storage.AppStorageManager;
import com.eaglesakura.android.db.DaoDatabase;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Initializer;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.StringUtil;

import org.greenrobot.greendao.database.StandardDatabase;
import org.greenrobot.greendao.query.QueryBuilder;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Central Engineの設定データを保持するDB
 */
public class CentralSettingDatabase extends DaoDatabase<DaoSession> {

    static final int SUPPORTED_DATABASE_VERSION = 1;

    private static final String GROUP_NAME_DEFAULT = "null";

    @Inject(AppStorageProvider.class)
    AppStorageManager mStorageController;

    public CentralSettingDatabase(Context context) {
        super(context, DaoMaster.class);
    }

    @Initializer
    public void initialize() {
        Garnet.inject(this);
    }

    /**
     * コマンドを保存する
     */
    public void update(@NonNull DbCommand cmd) {
        session.insertOrReplace(cmd);
    }

    /**
     * 指定したコマンドを削除する
     */
    public void remove(@NonNull CommandKey key) {
        session.getDbCommandDao().deleteByKey(key.toString());
    }

    /**
     * 指定したカテゴリのコマンドを列挙する
     *
     * @param category 列挙するコマンド
     * @see CommandData#CATEGORY_PROXIMITY
     * @see CommandData#CATEGORY_SPEED
     * @see CommandData#CATEGORY_TIMER
     * @see CommandData#CATEGORY_DISTANCE
     */
    @NonNull
    public List<CommandData> list(int category) {
        List<DbCommand> commands = session.getDbCommandDao().queryBuilder()
                .where(DbCommandDao.Properties.Category.eq(category))
                .list();

        return CollectionUtil.asOtherList(commands, it -> new CommandData(it));
    }

    /**
     * 表示対象に関連付けられたレイアウト設定を列挙する
     */
    public List<DbDisplayLayout> listLayouts(DbDisplayTarget target) {
        return session.getDbDisplayLayoutDao().queryBuilder()
                .where(DbDisplayLayoutDao.Properties.AppPackageName.eq(target.getTargetPackage()))
                .orderAsc(DbDisplayLayoutDao.Properties.SlotId)
                .list();
    }

    /**
     * レイアウト設定を更新する
     */
    public void update(DbDisplayLayout layout) {
        session.insertOrReplace(layout);
    }

    /**
     * レイアウト設定を削除し、スロットを空ける
     */
    public void remove(DbDisplayLayout layout) {
        session.delete(layout);
    }


    /**
     * 表示対象のグループを削除する。
     */
    public void remove(final String appPackageName) {
        session.runInTx(() -> {
            // グループレイアウトを削除する
            QueryBuilder<DbDisplayLayout> builder = session.getDbDisplayLayoutDao().queryBuilder();
            builder.where(DbDisplayLayoutDao.Properties.AppPackageName.eq(appPackageName));
            builder.buildDelete().executeDeleteWithoutDetachingEntities();
        });
    }

    /**
     * スキャン済みのデバイスを取得する
     */
    public List<DbBleFitnessDevice> listScanDevices(FitnessDeviceType device) {
        QueryBuilder<DbBleFitnessDevice> queryBuilder = session.getDbBleFitnessDeviceDao().queryBuilder();
        return queryBuilder
                .where(DbBleFitnessDeviceDao.Properties.DeviceType.eq(device.getDeviceTypeId()))
                .orderAsc(DbBleFitnessDeviceDao.Properties.Address) // アドレス順に並べる
                .list();
    }

    /**
     * アドレスを指定して取得する
     */
    public DbBleFitnessDevice load(String address) {
        return session.getDbBleFitnessDeviceDao().load(address);
    }

    /**
     * 情報を更新する
     */
    public void update(DbBleFitnessDevice device) {
        session.getDbBleFitnessDeviceDao().update(device);
    }

    /**
     * デバイスを検出した
     */
    public void foundDevice(FitnessDeviceType type, BleDevice device) {
        DbBleFitnessDevice dbDevice = load(device.getAddress());
        if (dbDevice == null) {
            dbDevice = new DbBleFitnessDevice();
            dbDevice.setDeviceType(type.getDeviceTypeId());
            dbDevice.setAddress(device.getAddress());
            session.getDbBleFitnessDeviceDao().insert(dbDevice);
        }
    }

    static String getPluginClassName(String packageName, String className) {
        return StringUtil.format("%s@%s", packageName, className);
    }

    public void activePlugin(PluginInformation pluginInfo, ResolveInfo appInfo) {
        DbActivePlugin dbActivePlugin = new DbActivePlugin();
        dbActivePlugin.setUniqueId(getPluginClassName(appInfo.serviceInfo.packageName, appInfo.serviceInfo.name));
        dbActivePlugin.setCategory(pluginInfo.getCategory().getName());
        dbActivePlugin.setPackageName(appInfo.serviceInfo.packageName);
        dbActivePlugin.setClassName(appInfo.serviceInfo.name);

        session.getDbActivePluginDao().insertOrReplace(dbActivePlugin);
    }

    public boolean isActivePlugin(String packageName, String className) {
        String key = getPluginClassName(packageName, className);
        return session.getDbActivePluginDao().queryBuilder()
                .where(DbActivePluginDao.Properties.UniqueId.eq(key))
                .count() > 0;
    }

    /**
     * 指定した内蔵クラスを強制的にアクティブにする
     */
    public void activePlugin(Class clazz, Category category) {
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
    public void disablePlugin(ResolveInfo appInfo) {
        session.getDbActivePluginDao().deleteByKey(getPluginClassName(appInfo.serviceInfo.packageName, appInfo.serviceInfo.name));
    }

    /**
     * プラグインを無効化する
     */
    public void disablePlugin(String packageName, String className) {
        session.getDbActivePluginDao().deleteByKey(getPluginClassName(packageName, className));
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
    public ActivePluginCollection listPlugins() {
        return new ActivePluginCollection(
                CollectionUtil.asOtherList(
                        session.getDbActivePluginDao().loadAll(),
                        it -> new ActivePlugin(it)
                )
        );
    }

    @Override
    protected SQLiteOpenHelper createHelper() {
        return new SQLiteOpenHelper(getContext(), mStorageController.getExternalDatabasePath("v3_commands.db").getAbsolutePath(), null, SUPPORTED_DATABASE_VERSION) {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                DaoMaster.createAllTables(new StandardDatabase(db), false);
            }
        };
    }
}
