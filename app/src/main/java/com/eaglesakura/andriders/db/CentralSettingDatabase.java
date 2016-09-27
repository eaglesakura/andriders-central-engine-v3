package com.eaglesakura.andriders.db;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.dao.central.DaoMaster;
import com.eaglesakura.andriders.dao.central.DaoSession;
import com.eaglesakura.andriders.dao.central.DbCommand;
import com.eaglesakura.andriders.dao.central.DbCommandDao;
import com.eaglesakura.andriders.dao.central.DbDisplayLayout;
import com.eaglesakura.andriders.dao.central.DbDisplayLayoutDao;
import com.eaglesakura.andriders.dao.central.DbDisplayTarget;
import com.eaglesakura.andriders.dao.central.DbDisplayTargetDao;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.provider.AppControllerProvider;
import com.eaglesakura.android.db.DaoDatabase;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.StringUtil;

import org.greenrobot.greendao.database.StandardDatabase;
import org.greenrobot.greendao.query.QueryBuilder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

/**
 * Central Engineの設定データを保持するDB
 */
public class CentralSettingDatabase extends DaoDatabase<DaoSession> {

    /**
     * 近接コマンド
     */
    public static final int CATEGORY_PROXIMITY = 1;

    /**
     * 速度コマンド
     */
    public static final int CATEGORY_SPEED = 2;

    /**
     * 距離コマンド
     */
    public static final int CATEGORY_DISTANCE = 3;

    /**
     * タイマーコマンド
     */
    public static final int CATEGORY_TIMER = 4;

    static final int SUPPORTED_DATABASE_VERSION = 1;

    private static final String PACKAGE_NAME_DEFAULT = "null";

    private static final String GROUP_NAME_DEFAULT = "null";

    @Inject(AppControllerProvider.class)
    AppStorageController mStorageController;

    public CentralSettingDatabase(Context context) {
        super(context, DaoMaster.class);
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
        session.getDbCommandDao().deleteByKey(key.getKey());
    }

    /**
     * 指定したカテゴリのコマンドを列挙する
     *
     * @param category 列挙するコマンド
     * @see #CATEGORY_PROXIMITY
     * @see #CATEGORY_SPEED
     * @see #CATEGORY_TIMER
     * @see #CATEGORY_DISTANCE
     */
    @NonNull
    public List<CommandData> list(int category) {
        List<DbCommand> commands = session.getDbCommandDao().queryBuilder()
                .where(DbCommandDao.Properties.Category.eq(category))
                .list();

        return CollectionUtil.asOtherList(commands, it -> new CommandData(it));
    }


    private String getDisplayTargetKey(@Nullable String appPackageName) {
        if (StringUtil.isEmpty(appPackageName)) {
            appPackageName = BuildConfig.APPLICATION_ID;
        }
        return "target:" + appPackageName;
    }

    /**
     * レイアウト用のグループを取得する。
     * DBに登録されていない場合は新規に作成して返す。
     *
     * @param packageName 表示対象のpackage名 / null可
     */
    private DbDisplayTarget loadTargetOrCreate(@Nullable String packageName) {
        String uniqueId = getDisplayTargetKey(packageName);

        DbDisplayTarget result = session.getDbDisplayTargetDao().load(uniqueId);
        // DBがなければ作成して返す
        if (result == null) {
            result = new DbDisplayTarget(uniqueId);
            result.setCreatedDate(new Date());
            result.setModifiedDate(new Date());
            result.setLayoutType(0);
            result.setName(GROUP_NAME_DEFAULT);
            result.setTargetPackage(uniqueId);

            session.insert(result);
        }
        return result;
    }

    private DbDisplayTarget loadTarget(@Nullable String packageName) {
        String uniqueId = getDisplayTargetKey(packageName);
        DbDisplayTarget result = session.getDbDisplayTargetDao().load(uniqueId);
        // DBがなければ作成して返すが、insertは行わない
        if (result == null) {
            result = new DbDisplayTarget(uniqueId);
            result.setCreatedDate(new Date());
            result.setModifiedDate(new Date());
            result.setLayoutType(0);
            result.setName(GROUP_NAME_DEFAULT);
            result.setTargetPackage(packageName);
        }
        return result;
    }

    /**
     * 保存されているターゲット一覧を返す
     *
     * 最も遅くに保存された順番に返す。最新の変更ほど先に来ることになる。
     */
    public List<DbDisplayTarget> listTargets() {
        QueryBuilder<DbDisplayTarget> builder = session.getDbDisplayTargetDao().queryBuilder();
        return builder
                .orderDesc(DbDisplayTargetDao.Properties.ModifiedDate)
                .list();
    }

    /**
     * 保存されているターゲット数を取得する
     */
    public int getTargetCount() {
        return (int) session.getDbDisplayTargetDao().count();
    }

    /**
     * 表示対象に関連付けられたレイアウト設定を列挙する
     */
    public List<DbDisplayLayout> listLayouts(DbDisplayTarget target) {
        return session.getDbDisplayLayoutDao().queryBuilder()
                .where(DbDisplayLayoutDao.Properties.TargetPackage.eq(target.getTargetPackage()))
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
     * レイアウト設定を削除し、スロットを空ける
     */
    public void remove(DbDisplayTarget target, int slotId) {
        QueryBuilder<DbDisplayLayout> builder = session.getDbDisplayLayoutDao().queryBuilder();
        builder
                .where(DbDisplayLayoutDao.Properties.TargetPackage.eq(target.getTargetPackage()),
                        DbDisplayLayoutDao.Properties.SlotId.eq(slotId))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    /**
     * 表示対象のグループを削除する。
     */
    public void remove(final String appPackageName) {
        session.runInTx(() -> {
            // グループレイアウトを削除する
            {
                QueryBuilder<DbDisplayLayout> builder = session.getDbDisplayLayoutDao().queryBuilder();
                builder.where(DbDisplayLayoutDao.Properties.TargetPackage.eq(appPackageName));
                builder.buildDelete().executeDeleteWithoutDetachingEntities();
            }

            // グループ管理を削除する
            session.getDbDisplayTargetDao().deleteByKey(getDisplayTargetKey(appPackageName));
        });
    }

    /**
     * 表示対象のグループを削除する。
     */
    public void remove(final DbDisplayTarget target) {
        remove(target.getTargetPackage());
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
