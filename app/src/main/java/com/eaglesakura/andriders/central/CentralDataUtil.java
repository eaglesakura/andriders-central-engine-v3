package com.eaglesakura.andriders.central;

import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.lambda.Action1;

public class CentralDataUtil {

    /**
     * CentralDataが有効であれば、処理を実行する
     */
    public static void execute(CentralDataHolder holder, Action1<CentralDataManager> action) {
        try {
            CentralDataManager dataManager = (holder == null ? null : holder.getCentralDataManager());
            if (dataManager == null) {
                return;
            }

            action.action(dataManager);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
