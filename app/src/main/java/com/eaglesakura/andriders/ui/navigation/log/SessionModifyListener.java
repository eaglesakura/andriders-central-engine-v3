package com.eaglesakura.andriders.ui.navigation.log;

import java.util.List;

/**
 * セッション情報更新に対応する
 */
public interface SessionModifyListener {
    /**
     * 指定日のセッションが削除された
     *
     * @param sessions 削除されたセッション一覧
     */
    void onDeleteSession(List<Long> sessions);
}
