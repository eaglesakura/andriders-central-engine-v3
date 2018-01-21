package com.eaglesakura.andriders.central.service;

/**
 * CentralSessionの現在の状態を管理する
 */
public class SessionState {

    private final State mState;

    public enum State {
        NewObject,

        Initializing,

        Running,

        Stopping,

        Destroyed,

        Error,
    }

    SessionState(State state) {
        mState = state;
    }

    public State getState() {
        return mState;
    }
}
