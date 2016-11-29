package com.eaglesakura.andriders.central;

import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.data.display.DisplayBindManager;
import com.eaglesakura.andriders.data.notification.CentralNotificationManager;
import com.eaglesakura.lambda.Action1;

public class CentralDataUtil {

    /**
     * Managerが有効であれば、処理を実行する
     */
    public static void execute(CentralDataManager.Holder holder, Action1<CentralDataManager> action) {
        try {
            CentralDataManager manager = (holder == null ? null : holder.getCentralDataManager());
            if (manager == null) {
                return;
            }

            action.action(manager);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Managerが有効であれば処理を実行する
     */
    public static void execute(CentralNotificationManager.Holder holder, Action1<CentralNotificationManager> action) {
        try {
            CentralNotificationManager manager = (holder == null ? null : holder.getCentralNotificationManager());
            if (manager == null) {
                return;
            }

            action.action(manager);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Managerが有効であれば処理を実行する
     */
    public static void execute(DisplayBindManager.Holder holder, Action1<DisplayBindManager> action) {
        try {
            DisplayBindManager manager = (holder == null ? null : holder.getDisplayBindManager());
            if (manager == null) {
                return;
            }

            action.action(manager);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


}
