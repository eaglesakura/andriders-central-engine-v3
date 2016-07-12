package com.eaglesakura.andriders.db.command;

import com.android.annotations.NonNull;
import com.eaglesakura.andriders.dao.command.DaoMaster;
import com.eaglesakura.andriders.dao.command.DaoSession;
import com.eaglesakura.andriders.dao.command.DbCommand;
import com.eaglesakura.andriders.dao.command.DbCommandDao;
import com.eaglesakura.android.db.DaoDatabase;
import com.eaglesakura.util.CollectionUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.IntRange;

import java.util.List;

public class CommandDatabase extends DaoDatabase<DaoSession> {
    private static final int SUPPORTED_DATABASE_VERSION = 1;


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

    public CommandDatabase(Context context) {
        super(context, DaoMaster.class);
    }

    /**
     * 指定したカテゴリのコマンドを列挙する
     *
     * @param category 列挙するコマンド
     */
    @NonNull
    public List<CommandData> list(@IntRange(from = CATEGORY_PROXIMITY, to = CATEGORY_DISTANCE) int category) {
        List<DbCommand> commands = session.getDbCommandDao().queryBuilder()
                .where(DbCommandDao.Properties.Category.eq(category))
                .list();

        return CollectionUtil.asOtherList(commands, it -> new CommandData(it));
    }

    @Override
    protected SQLiteOpenHelper createHelper() {
        return new SQLiteOpenHelper(context, context.getDatabasePath("commands.db").getAbsolutePath(), null, SUPPORTED_DATABASE_VERSION) {
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
