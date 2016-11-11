package com.eaglesakura.andriders.ui.navigation.command;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.lifecycle.UiLifecycleDelegate;
import com.eaglesakura.util.StringUtil;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

public class CommandEditDialogBuilder {
    final AlertDialog.Builder mDialogBuilder;

    final CommandData mCommandData;

    OnCommitListener mCommitListener;

    OnDeleteListener mDeleteListener;

    CommandEditDialogBuilder(AlertDialog.Builder builder, CommandData commandData) {
        mCommandData = commandData;
        mDialogBuilder = builder;
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
        return lifecycleDelegate.addAutoDismiss(mDialogBuilder.show());
    }

    /**
     * 距離コマンド用のデータを構築する
     */
    static CommandEditDialogBuilder fromDistance(Context context, CommandData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View content = LayoutInflater.from(context).inflate(R.layout.command_setup_distance_dialog, null, false);
        CommandData.Extra extra = data.getInternalExtra();
        new AQuery(content)
                .id(R.id.Command_Distance_Type).setSelection(data.getInternalExtra().distanceType)
                .id(R.id.Command_Distance_Text).text(StringUtil.format("%f", extra.distanceKm))
                .id(R.id.Command_Distance_Repeat).checked((extra.flags & CommandData.DISTANCE_FLAG_REPEAT) != 0)
                .id(R.id.Command_Distance_ActiveOnly).checked((extra.flags & CommandData.DISTANCE_FLAG_ACTIVE_ONLY) != 0)
        ;

        return new CommandEditDialogBuilder(builder, data);
    }

    public static CommandEditDialogBuilder from(Context context, CommandData commandData) {
        CommandEditDialogBuilder result;
        switch (commandData.getCategory()) {
            case CommandData.CATEGORY_DISTANCE:
                result = fromDistance(context, commandData);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Category :: " + commandData.getCategory());
        }
        result.mDialogBuilder.setTitle("条件設定");
        result.mDialogBuilder.setPositiveButton("保存", (dialog, which) -> {
            if (result.mCommitListener != null) {
                result.mCommitListener.onCommit(commandData);
            }
        });
        result.mDialogBuilder.setNeutralButton("削除", (dlg, which) -> {
            if (result.mDeleteListener != null) {
                result.mDeleteListener.onDelege(commandData);
            }
        });
        return result;
    }

    public interface OnCommitListener {
        void onCommit(CommandData data);
    }

    public interface OnDeleteListener {
        void onDelege(CommandData data);
    }
}
