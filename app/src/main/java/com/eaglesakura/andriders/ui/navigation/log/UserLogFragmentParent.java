package com.eaglesakura.andriders.ui.navigation.log;

import com.google.repacked.antlr.v4.runtime.misc.Nullable;

import com.eaglesakura.andriders.db.session.SessionTotalCollection;

public interface UserLogFragmentParent {

    /**
     * ログを取得する。
     *
     * ロードが未完了の場合はnullを返却する
     */
    @Nullable
    SessionTotalCollection getUserLogCollection();
}
