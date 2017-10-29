package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.service.FitnessCommitService;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.android.margarine.OnMenuClick;
import com.eaglesakura.sloth.annotation.BindInterface;
import com.eaglesakura.sloth.annotation.FragmentMenu;

import android.content.Intent;

import java.util.Date;


/**
 * GoogleFitへのアップロード管理を行う
 */
@FragmentMenu(R.menu.user_log_fit)
public class GoogleFitUploadMenuFragment extends AppFragment {

    @BindInterface
    Callback mCallback;

    @OnMenuClick(R.id.Menu_Upload_GoogleFit)
    void clickUploadGoogleFit() {
        long sessionId = mCallback.getUploadTargetSessionId(this);
        AppDialogBuilder.newInformation(getContext(), R.string.Message_Log_CheckUploadToGoogleFit)
                .title(LogSummaryBinding.DEFAULT_DAY_FORMATTER.format(new Date(sessionId)))
                .positiveButton(R.string.Word_Common_OK, () -> {
                    Intent intent = FitnessCommitService.Builder.from(getContext())
                            .session(sessionId)
                            .build();
                    getContext().startService(intent);
                })
                .negativeButton(R.string.Word_Common_Cancel, null)
                .show(getFragmentLifecycle());
    }

    public interface Callback {
        long getUploadTargetSessionId(GoogleFitUploadMenuFragment self);
    }
}
