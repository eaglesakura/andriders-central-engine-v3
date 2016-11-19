package com.eaglesakura.andriders.data.db;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.dao.central.DaoMaster;
import com.eaglesakura.andriders.dao.central.DaoSession;
import com.eaglesakura.andriders.dao.central.DbActivePlugin;
import com.eaglesakura.andriders.dao.central.DbActivePluginDao;
import com.eaglesakura.andriders.dao.central.DbBleSensor;
import com.eaglesakura.andriders.dao.central.DbBleSensorDao;
import com.eaglesakura.andriders.dao.central.DbCommand;
import com.eaglesakura.andriders.dao.central.DbCommandDao;
import com.eaglesakura.andriders.dao.central.DbDisplayLayout;
import com.eaglesakura.andriders.dao.central.DbDisplayLayoutDao;
import com.eaglesakura.andriders.model.ble.BleDeviceCache;
import com.eaglesakura.andriders.model.ble.BleDeviceCacheCollection;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.model.display.DisplayLayoutCollection;
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
import com.eaglesakura.collection.StringFlag;
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
    public List<CommandData> listCommands(int category) {
        List<DbCommand> commands = session.getDbCommandDao().queryBuilder()
                .where(DbCommandDao.Properties.Category.eq(category))
                .list();

        return CollectionUtil.asOtherList(commands, it -> new CommandData(it));
    }

    /**
     * すべてのレイアウト情報を列挙する
     */
    @NonNull
    public DisplayLayoutCollection listAllLayouts() {
        return new DisplayLayoutCollection(CollectionUtil.asOtherList(session.getDbDisplayLayoutDao().loadAll(), it -> new DisplayLayout(it)));
    }

    /**
     * 指定したpackageに所属するレイアウト情報を取得する
     *
     * 0件の場合は空リストを返す
     *
     * @param packageName アプリpackage名
     */
    @NonNull
    public DisplayLayoutCollection listLayouts(String packageName) {
        List<DbDisplayLayout> list = session.getDbDisplayLayoutDao().queryBuilder()
                .where(DbDisplayLayoutDao.Properties.AppPackageName.eq(packageName))
                .list();
        return new DisplayLayoutCollection(
                CollectionUtil.asOtherList(list, it -> new DisplayLayout(it))
        );
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
    public void removeLayouts(final String appPackageName) {
        session.runInTx(() -> {
            // グループレイアウトを削除する
            QueryBuilder<DbDisplayLayout> builder = session.getDbDisplayLayoutDao().queryBuilder();
            builder.where(DbDisplayLayoutDao.Properties.AppPackageName.eq(appPackageName));
            builder.buildDelete().executeDeleteWithoutDetachingEntities();
        });
    }

    /**
     * スキャン済みのBLEデバイスを保存する
     */
    public void save(BleDeviceCache device) {
        DbBleSensor sensor = new DbBleSensor();
        sensor.setAddress(device.getAddress());
        sensor.setName(device.getName());
        sensor.setTypeFlags(device.getFlags().toString());
        session.getDbBleSensorDao().insertOrReplace(sensor);
    }

    /**
     * スキャン済みのすべてのデバイスを列挙する
     */
    @NonNull
    public BleDeviceCacheCollection listBleDevices(int sensorType) {
        List<DbBleSensor> list = session.getDbBleSensorDao().queryBuilder()
                .where(DbBleSensorDao.Properties.TypeFlags.like(new StringFlag(sensorType).toLikeQuery()))
                .build()
                .list();

        return new BleDeviceCacheCollection(CollectionUtil.asOtherList(list, it -> new BleDeviceCache(it)));
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
        return new SQLiteOpenHelper(getContext(), mStorageController.getExternalDatabasePath("v3_settings.db").getAbsolutePath(), null, SUPPORTED_DATABASE_VERSION) {
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
