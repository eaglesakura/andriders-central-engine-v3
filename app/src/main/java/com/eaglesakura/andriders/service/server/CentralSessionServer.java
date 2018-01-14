package com.eaglesakura.andriders.service.server;

import com.eaglesakura.andriders.AceSdkUtil;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.plugin.internal.CentralServiceCommand;
import com.eaglesakura.andriders.serialize.RawSessionInfo;
import com.eaglesakura.android.service.CommandMap;
import com.eaglesakura.android.service.CommandServer;
import com.eaglesakura.android.service.data.Payload;
import com.eaglesakura.android.thread.UIHandler;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Central Session制御のサーバー動作を行う
 *
 * 別Processのクライアントから接続され、必要な処理を行う。
 */
public class CentralSessionServer {

    @NonNull
    final Callback mCallback;

    private CommandServerImpl mImpl;

    private CommandMap mSessionCommandMap = new CommandMap();

    public CentralSessionServer(@NonNull Service service, @NonNull Callback callback) {
        mCallback = callback;
        mImpl = new CommandServerImpl(service);
        buildServerCommand();
    }

    public IBinder getBinder(Intent intent) {
        if (CentralServiceCommand.ACTION_SESSION_CONTROL.equals(intent.getAction())) {
            return mImpl.getBinder();
        } else {
            return null;
        }
    }


    /**
     * 接続されているクライアントが存在する
     */
    public boolean hasClients() {
        return mImpl.getClientNum() > 0;
    }

    public interface Callback {
        @Nullable
        CentralSession getCurrentSession(CentralSessionServer self);
    }

    @Nullable
    CentralSession getCurrentSession() {
        return mCallback.getCurrentSession(this);
    }

    public Application getContext() {
        return mImpl.getService().getApplication();
    }

    public void notifyOnSessionStarted(SessionInfo info) {
        try {
            RawSessionInfo raw = new RawSessionInfo(info.getSessionId());
            mImpl.broadcast(CentralServiceCommand.CMD_onSessionStarted, new Payload(AceSdkUtil.serializeToByteArray(raw)));
        } catch (Exception e) {
        }
    }

    public void notifyOnSessionStopped(SessionInfo info) {
        try {
            RawSessionInfo raw = new RawSessionInfo(info.getSessionId());
            Payload payload = new Payload(AceSdkUtil.serializeToByteArray(raw));
            mImpl.broadcast(CentralServiceCommand.CMD_onSessionStopped, payload);
        } catch (Exception e) {

        }
    }

    private void buildServerCommand() {
        /**
         * セッションIDを取得する
         */
        mSessionCommandMap.addAction(CentralServiceCommand.CMD_getSessionInfo, (sender, cmd, payload) -> {
            CentralSession currentSession = getCurrentSession();
            if (currentSession == null) {
                return null;
            } else {
                RawSessionInfo info = new RawSessionInfo(currentSession.getSessionId());
                return new Payload(AceSdkUtil.serializeToByteArray(info));
            }
        });

        /**
         * セッションの開始を行う
         */
        mSessionCommandMap.addAction(CentralServiceCommand.CMD_requestSessionStart, (sender, cmd, payload) -> {
            CentralSession currentSession = getCurrentSession();
            if (currentSession != null) {
                throw new IllegalStateException("Session Started!!");
            }

            Intent intent = new Intent(CentralServiceCommand.ACTION_SESSION_START);
            intent.setComponent(CentralServiceCommand.COMPONENT_SESSION_SERVICE);

            UIHandler.postUI(() -> {
                getContext().startService(intent);
            });

            // 正常終了
            return null;
        });

        mSessionCommandMap.addAction(CentralServiceCommand.CMD_requestSessionStop, (sender, cmd, payload) -> {
            CentralSession session = getCurrentSession();
            if (session == null) {
                throw new IllegalStateException("Session Not Started!!");
            }

            Intent intent = new Intent(CentralServiceCommand.ACTION_SESSION_STOP);
            intent.setComponent(CentralServiceCommand.COMPONENT_SESSION_SERVICE);

            // セッション停止コマンドを流し、続いてServiceの起動モードをConnectのみに切り替える
            UIHandler.postUI(() -> {
                getContext().startService(intent);
            });
            UIHandler.postUI(() -> {
                getContext().stopService(intent);
            });

            return null;
        });
    }


    private class CommandServerImpl extends CommandServer {
        public CommandServerImpl(Service service) {
            super(service);
        }

        @Override
        protected Payload onReceivedDataFromClient(String cmd, String clientId, Payload payload) throws RemoteException {
            return mSessionCommandMap.execute(clientId, cmd, payload);
        }

        public void broadcast(String cmd, Payload payload) {
            try {
                super.broadcastToClient(cmd, payload);
            } catch (Exception e) {

            }
        }
    }
}
