package com.eaglesakura.andriders.service.server;

import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.plugin.internal.CentralServiceCommand;
import com.eaglesakura.andriders.serialize.RawSessionInfo;
import com.eaglesakura.android.service.CommandMap;
import com.eaglesakura.android.service.CommandServer;
import com.eaglesakura.android.service.data.Payload;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * セッション制御のサーバー動作を行う
 */
public class SessionServer {

    @NonNull
    final Callback mCallback;

    private CommandServerImpl mImpl;

    private CommandMap mSessionCommandMap = new CommandMap();

    public SessionServer(@NonNull Service service, @NonNull Callback callback) {
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

    public interface Callback {
        @Nullable
        CentralSession getCurrentSession(SessionServer self);
    }

    @Nullable
    public CentralSession getCurrentSession() {
        return mCallback.getCurrentSession(this);
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
                return Payload.fromPublicField(info);
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
                mImpl.getService().getApplication().startService(intent);
            });

            // 正常終了
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
    }
}
