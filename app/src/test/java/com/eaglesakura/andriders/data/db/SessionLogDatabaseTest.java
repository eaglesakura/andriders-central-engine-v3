package com.eaglesakura.andriders.data.db;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.provider.AppDatabaseProvider;
import com.eaglesakura.android.garnet.Garnet;

import org.junit.Test;

public class SessionLogDatabaseTest extends AppUnitTestCase {

    @Test
    public void 初回はログがnullとして取得される() throws Throwable {
        SessionLogDatabase db = Garnet.factory(AppDatabaseProvider.class).instance(SessionLogDatabase.class, AppDatabaseProvider.NAME_READ_ONLY);
        assertNotNull(db);

        try (SessionLogDatabase token = db.openWritable(SessionLogDatabase.class)) {
            // データが空である
            validate(token.getSession().getDbSessionPointDao().loadAll()).sizeIs(0);
            // 初回はnullが取得できる
            assertNull(token.loadTotal(0, 0, () -> false));
        }
    }
}