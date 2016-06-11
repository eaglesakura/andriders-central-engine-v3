package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.db.session.SessionTotalCollection;

import android.support.annotation.Nullable;

public interface UserLogFragmentParent {

    /**
     * ログを取得する。
     *
     * ロードが未完了の場合はnullを返却する
     */
    @Nullable
    SessionTotalCollection getUserLogCollection();
}
