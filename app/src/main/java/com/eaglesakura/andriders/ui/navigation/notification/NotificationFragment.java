package com.eaglesakura.andriders.ui.navigation.notification;

import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.framework.ui.UserNotificationController;

import android.content.Context;
import android.view.View;

public class NotificationFragment extends AppBaseFragment implements UserNotificationController.NotificationListener {

    Callback callback;

    UserNotificationController notificationController;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof Callback) {
            callback = (Callback) getActivity();
        } else if (getParentFragment() instanceof Callback) {
            callback = (Callback) getParentFragment();
        }

        notificationController = callback.getNotificationController(this);
        notificationController.setListener(this);
    }

    @Override
    public void onDetach() {
        callback.onNotificationDetatched(this, notificationController);
        notificationController.setListener(null);
        notificationController = null;
        super.onDetach();
    }

    @Override
    public boolean onVisibleProgress(UserNotificationController controller, Object sender, String message) {
        return false;
    }

    @Override
    public boolean onUpdateProgress(UserNotificationController controller, int refs, Object sender, String message) {
        return false;
    }

    @Override
    public boolean onDismissProgress(UserNotificationController controller, Object sender) {
        return false;
    }

    public interface Callback {
        UserNotificationController getNotificationController(NotificationFragment self);

        void onNotificationDetatched(NotificationFragment self, UserNotificationController controller);
    }
}
