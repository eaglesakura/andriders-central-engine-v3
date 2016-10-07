package com.eaglesakura.andriders.service;

import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.plugin.internal.CentralServiceCommand;
import com.eaglesakura.andriders.service.server.SessionServer;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.util.ContextUtil;

import org.greenrobot.greendao.annotation.NotNull;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 *
 */
public class CentralSessionService extends Service {

    @Nullable
    CentralSession mSession;

    @NotNull
    final SessionServer mCentralSessionServer;

    public CentralSessionService() {
        AppLog.system("NewService");
        mCentralSessionServer = new SessionServer(this, mSessionServerCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AppLog.system("onBind");
        return mCentralSessionServer.getBinder(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppLog.system("onStartCommand");
        if (CentralServiceCommand.ACTION_SESSION_START.equals(intent.getAction())) {
            // セッションを開始させる
            if (mSession == null) {
                mSession = startNewSession(intent);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mSession != null) {
            mSession.unregisterCallback(mCentralSessionListener);
            mSession.dispose();
            mSession = null;
        }
        super.onDestroy();
    }

    /**
     * 新規セッションを開始する
     */
    protected CentralSession startNewSession(Intent intent) {
        SessionInfo sessionInfo = new SessionInfo.Builder(this, new Clock(System.currentTimeMillis()))
                .debugable(intent.getBooleanExtra(CentralServiceCommand.EXTRA_BOOT_DEBUG_MODE, false))
                .build();

        CentralSession centralSession = CentralSession.newInstance(sessionInfo);
        centralSession.registerCallback(mCentralSessionListener);

        CentralSession.InitializeOption option = new CentralSession.InitializeOption();
        centralSession.initialize(option);

        return centralSession;
    }

    private CentralSession.Listener mCentralSessionListener = new CentralSession.Listener() {
        @Override
        public void onInitializeCompleted(CentralSession self) {

        }
    };

    private SessionServer.Callback mSessionServerCallback = new SessionServer.Callback() {
        @Override
        @Nullable
        public CentralSession getCurrentSession(SessionServer self) {
            return mSession;
        }
    };

    public static boolean isRunning(Context context) {
        return ContextUtil.isServiceRunning(context, CentralSessionService.class);
    }

}
