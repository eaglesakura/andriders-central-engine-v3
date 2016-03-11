package com.eaglesakura.andriders.dao.session;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.eaglesakura.andriders.dao.session.DbCycleDateLog;
import com.eaglesakura.andriders.dao.session.DbSessionLog;
import com.eaglesakura.andriders.dao.session.DbSessionPoint;

import com.eaglesakura.andriders.dao.session.DbCycleDateLogDao;
import com.eaglesakura.andriders.dao.session.DbSessionLogDao;
import com.eaglesakura.andriders.dao.session.DbSessionPointDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig dbCycleDateLogDaoConfig;
    private final DaoConfig dbSessionLogDaoConfig;
    private final DaoConfig dbSessionPointDaoConfig;

    private final DbCycleDateLogDao dbCycleDateLogDao;
    private final DbSessionLogDao dbSessionLogDao;
    private final DbSessionPointDao dbSessionPointDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        dbCycleDateLogDaoConfig = daoConfigMap.get(DbCycleDateLogDao.class).clone();
        dbCycleDateLogDaoConfig.initIdentityScope(type);

        dbSessionLogDaoConfig = daoConfigMap.get(DbSessionLogDao.class).clone();
        dbSessionLogDaoConfig.initIdentityScope(type);

        dbSessionPointDaoConfig = daoConfigMap.get(DbSessionPointDao.class).clone();
        dbSessionPointDaoConfig.initIdentityScope(type);

        dbCycleDateLogDao = new DbCycleDateLogDao(dbCycleDateLogDaoConfig, this);
        dbSessionLogDao = new DbSessionLogDao(dbSessionLogDaoConfig, this);
        dbSessionPointDao = new DbSessionPointDao(dbSessionPointDaoConfig, this);

        registerDao(DbCycleDateLog.class, dbCycleDateLogDao);
        registerDao(DbSessionLog.class, dbSessionLogDao);
        registerDao(DbSessionPoint.class, dbSessionPointDao);
    }
    
    public void clear() {
        dbCycleDateLogDaoConfig.getIdentityScope().clear();
        dbSessionLogDaoConfig.getIdentityScope().clear();
        dbSessionPointDaoConfig.getIdentityScope().clear();
    }

    public DbCycleDateLogDao getDbCycleDateLogDao() {
        return dbCycleDateLogDao;
    }

    public DbSessionLogDao getDbSessionLogDao() {
        return dbSessionLogDao;
    }

    public DbSessionPointDao getDbSessionPointDao() {
        return dbSessionPointDao;
    }

}
