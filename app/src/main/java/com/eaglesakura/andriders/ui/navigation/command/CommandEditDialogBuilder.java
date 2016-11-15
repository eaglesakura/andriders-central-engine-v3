package com.eaglesakura.andriders.ui.navigation.command;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.lifecycle.UiLifecycleDelegate;
import com.eaglesakura.android.ui.spinner.BasicSpinnerAdapter;
import com.eaglesakura.material.widget.SpinnerAdapterBuilder;
import com.eaglesakura.util.StringUtil;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

public class CommandEditDialogBuilder {
    final AlertDialog.Builder mDialogBuilder;

    final CommandData mCommandData;

    final View mContent;

    OnCommitListener mCommitListener = (view, data) -> {
    };

    OnDeleteListener mDeleteListener = data -> {
    };

    CommandEditDialogBuilder(AlertDialog.Builder builder, View content, CommandData commandData) {
        mCommandData = commandData;
        mDialogBuilder = builder;
        mContent = content;
    }

    public CommandEditDialogBuilder commit(OnCommitListener commitListener) {
        mCommitListener = commitListener;
        return this;
    }

    public CommandEditDialogBuilder delete(OnDeleteListener deleteListener) {
        mDeleteListener = deleteListener;
        return this;
    }


    public Dialog show(UiLifecycleDelegate lifecycleDelegate) {
        AlertDialog dialog = mDialogBuilder.show();
        return lifecycleDelegate.addAutoDismiss(dialog);
    }

    /**
     * 距離コマンド用のデータを構築する
     */
    static CommandEditDialogBuilder fromDistance(Context context, CommandData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = LayoutInflater.from(context).inflate(R.layout.command_setup_distance_dialog, null, false);
        CommandData.Extra extra = data.getInternalExtra();
        new AQuery(view)
                .id(R.id.Selector_Type).setSelection(data.getInternalExtra().distanceType)
                .id(R.id.Item_Value).text(String.valueOf(extra.distanceKm))
                .id(R.id.Button_Repeat).checked((extra.flags & CommandData.DISTANCE_FLAG_REPEAT) != 0)
                .id(R.id.Button_ActiveOnly).checked((extra.flags & CommandData.DISTANCE_FLAG_ACTIVE_ONLY) != 0)
        ;

        return new CommandEditDialogBuilder(builder.setView(view), view, data);
    }

    static CommandEditDialogBuilder fromSpeed(Context context, CommandData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.command_setup_speed_dialog, null, false);
        CommandData.Extra extra = data.getInternalExtra();

        AQuery q = new AQuery(view);
        q.id(R.id.Item_Value).text(String.valueOf(Math.max(extra.speedKmh, 1f)));

        String[] footers = view.getResources().getStringArray(R.array.Command_Speed_TypeFooter);
        SpinnerAdapterBuilder.fromStringArray(q.id(R.id.Selector_Type).getSpinner(), context, R.array.Command_Speed_TypeSelector)
                .selected((position, text) -> {
                    String footer = footers[position];
                    if (StringUtil.isEmpty(footer)) {
                        q.id(R.id.Content_SpeedEditRoot).gone();
                    } else {
                        q
                                .id(R.id.Item_Footer).text(footer)
                                .id(R.id.Content_SpeedEditRoot).visible();
                    }
                })
                .build();

        return new CommandEditDialogBuilder(builder.setView(view), view, data);
    }

    static CommandEditDialogBuilder fromTimer(Context context, CommandData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.command_setup_timer_dialog, null, false);
        CommandData.Extra extra = data.getInternalExtra();

        AQuery q = new AQuery(view);


        SpinnerAdapterBuilder.fromStringArray(q.id(R.id.Selector_Type).getSpinner(), context, R.array.Command_Timer_TypeSelector).build();
        q
                .id(R.id.Selector_Type).setSelection(data.getInternalExtra().timerType)
                .id(R.id.Item_Value).text(String.valueOf(extra.timerIntervalSec))
                .id(R.id.Button_Repeat).checked((extra.flags & CommandData.TIMER_FLAG_REPEAT) != 0)
        ;

        return new CommandEditDialogBuilder(builder.setView(view), view, data);
    }

    public static CommandEditDialogBuilder from(Context context, CommandData commandData) {
        CommandEditDialogBuilder result;
        switch (commandData.getCategory()) {
            case CommandData.CATEGORY_DISTANCE:
                result = fromDistance(context, commandData);
                break;
            case CommandData.CATEGORY_SPEED:
                result = fromSpeed(context, commandData);
                break;
            case CommandData.CATEGORY_TIMER:
                result = fromTimer(context, commandData);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Category :: " + commandData.getCategory());
        }
        result.mDialogBuilder.setTitle(R.string.Title_Command_Edit);
        result.mDialogBuilder.setPositiveButton(R.string.Word_Common_Save, (dialog, which) -> result.mCommitListener.onCommit(result.mContent, commandData));
        result.mDialogBuilder.setNeutralButton(R.string.Word_Common_Delete, (dlg, which) -> result.mDeleteListener.onDelete(commandData));
        return result;
    }

    public interface OnCommitListener {
        void onCommit(View view, CommandData data);
    }

    public interface OnDeleteListener {
        void onDelete(CommandData data);
    }
}
