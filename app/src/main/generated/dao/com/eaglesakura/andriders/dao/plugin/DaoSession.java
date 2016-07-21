package com.eaglesakura.andriders.dao.plugin;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.eaglesakura.andriders.dao.plugin.DbActivePlugin;

import com.eaglesakura.andriders.dao.plugin.DbActivePluginDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig dbActivePluginDaoConfig;

    private final DbActivePluginDao dbActivePluginDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        dbActivePluginDaoConfig = daoConfigMap.get(DbActivePluginDao.class).clone();
        dbActivePluginDaoConfig.initIdentityScope(type);

        dbActivePluginDao = new DbActivePluginDao(dbActivePluginDaoConfig, this);

        registerDao(DbActivePlugin.class, dbActivePluginDao);
    }
    
    public void clear() {
        dbActivePluginDaoConfig.getIdentityScope().clear();
    }

    public DbActivePluginDao getDbActivePluginDao() {
        return dbActivePluginDao;
    }

}
