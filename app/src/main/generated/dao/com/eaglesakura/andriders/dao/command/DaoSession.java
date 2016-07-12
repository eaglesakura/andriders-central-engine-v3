package com.eaglesakura.andriders.dao.command;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.eaglesakura.andriders.dao.command.DbCommand;

import com.eaglesakura.andriders.dao.command.DbCommandDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig dbCommandDaoConfig;

    private final DbCommandDao dbCommandDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        dbCommandDaoConfig = daoConfigMap.get(DbCommandDao.class).clone();
        dbCommandDaoConfig.initIdentityScope(type);

        dbCommandDao = new DbCommandDao(dbCommandDaoConfig, this);

        registerDao(DbCommand.class, dbCommandDao);
    }
    
    public void clear() {
        dbCommandDaoConfig.getIdentityScope().clear();
    }

    public DbCommandDao getDbCommandDao() {
        return dbCommandDao;
    }

}
