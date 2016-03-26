package com.eaglesakura.andriders.db.display;

import com.eaglesakura.andriders.dao.display.DaoMaster;
import com.eaglesakura.andriders.dao.display.DaoSession;
import com.eaglesakura.andriders.dao.display.DbDisplayLayout;
import com.eaglesakura.andriders.dao.display.DbDisplayLayoutDao;
import com.eaglesakura.andriders.dao.display.DbDisplayTarget;
import com.eaglesakura.andriders.dao.display.DbDisplayTargetDao;
import com.eaglesakura.android.db.DaoDatabase;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 *
 */
public class DisplayLayoutDatabase extends DaoDatabase<DaoSession> {
    private static final int SUPPORTED_DATABASE_VERSION = 0x01;

    private static final String PACKAGE_NAME_DEFAULT = "null";

    private static final String GROUP_NAME_DEFAULT = "null";

    public DisplayLayoutDatabase(Context context) {
        super(context, DaoMaster.class);
    }

    private static String wrapPackageName(String packageName) {
        return StringUtil.isEmpty(packageName) ? PACKAGE_NAME_DEFAULT : packageName;
    }

    /**
     * レイアウト用のグループを取得する。
     * DBに登録されていない場合は新規に作成して返す。
     *
     * @param packageName 表示対象のpackage名 / null可
     */
    public DbDisplayTarget loadTargetOrCreate(@Nullable String packageName) {
        packageName = wrapPackageName(packageName);
        String uniqueId = "target:" + packageName;

        DbDisplayTarget result = session.getDbDisplayTargetDao().load(uniqueId);
        // DBがなければ作成して返す
        if (result == null) {
            result = new DbDisplayTarget(uniqueId);
            result.setCreatedDate(new Date());
            result.setModifiedDate(new Date());
            result.setLayoutType(0);
            result.setName(GROUP_NAME_DEFAULT);
            result.setTargetPackage(packageName);

            session.insert(result);
        }
        return result;
    }

    public DbDisplayTarget loadTarget(@Nullable String packageName) {
        packageName = wrapPackageName(packageName);
        String uniqueId = "target:" + packageName;

        DbDisplayTarget result = session.getDbDisplayTargetDao().load(uniqueId);
        // DBがなければ作成して返すが、insertは行わない
        if (result == null) {
            result = new DbDisplayTarget("target:" + PACKAGE_NAME_DEFAULT);
            result.setCreatedDate(new Date());
            result.setModifiedDate(new Date());
            result.setLayoutType(0);
            result.setName(GROUP_NAME_DEFAULT);
            result.setTargetPackage(PACKAGE_NAME_DEFAULT);
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
    public void remove(final DbDisplayTarget target) {
        if (StringUtil.isEmpty(target.getTargetPackage())) {
            throw new IllegalArgumentException();
        }

        session.runInTx(() -> {
            // グループレイアウトを削除する
            {
                QueryBuilder<DbDisplayLayout> builder = session.getDbDisplayLayoutDao().queryBuilder();
                builder.where(DbDisplayLayoutDao.Properties.TargetPackage.eq(target.getTargetPackage()));
                builder.buildDelete().executeDeleteWithoutDetachingEntities();
            }

            // グループ管理を削除する
            session.getDbDisplayTargetDao().delete(target);
        });
    }

    @Override
    protected SQLiteOpenHelper createHelper() {
        return new SQLiteOpenHelper(context, "display_layout.db", null, SUPPORTED_DATABASE_VERSION) {

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
