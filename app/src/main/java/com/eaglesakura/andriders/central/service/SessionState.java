package com.eaglesakura.andriders.central.service;

import com.eaglesakura.sloth.data.DataBus;

import android.support.annotation.Nullable;

/**
 * CentralSessionの現在の状態を管理する
 */
public class SessionState {

    private final State mState;

    private final CentralSession mSession;

    public enum State {
        NewObject,

        Initializing,

        Running,

        Stopping,

        Destroyed,

        Error,
    }

    SessionState(State state, CentralSession session) {
        mState = state;
        mSession = session;
    }

    public State getState() {
        return mState;
    }

    public CentralSession getSession() {
        return mSession;
    }

    public static class Bus extends DataBus<SessionState> {
        Bus(@Nullable SessionState data) {
            super(data);
        }

        public State getState() {
            return getData().getState();
        }

        public CentralSession getSession() {
            return getData().getSession();
        }
    }
}
