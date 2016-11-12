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

        View content = LayoutInflater.from(context).inflate(R.layout.command_setup_distance_dialog, null, false);
        CommandData.Extra extra = data.getInternalExtra();
        new AQuery(content)
                .id(R.id.Selector_Type).setSelection(data.getInternalExtra().distanceType)
                .id(R.id.Item_Value).text(StringUtil.format("%f", extra.distanceKm))
                .id(R.id.Button_Repeat).checked((extra.flags & CommandData.DISTANCE_FLAG_REPEAT) != 0)
                .id(R.id.Button_ActiveOnly).checked((extra.flags & CommandData.DISTANCE_FLAG_ACTIVE_ONLY) != 0)
        ;

        return new CommandEditDialogBuilder(builder.setView(content), content, data);
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
