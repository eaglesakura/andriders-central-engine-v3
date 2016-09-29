package com.eaglesakura.andriders.ui.navigation.command;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.command.CommandDataCollection;
import com.eaglesakura.andriders.plugin.CommandDataManager;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.material.widget.MaterialAlertDialog;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.material.widget.support.SupportRecyclerView;

import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;

import java.util.List;

/**
 * コマンド設定のリスト表記を行う
 */
public abstract class CommandBaseFragment extends AppBaseFragment {
    @Bind(R.id.Command_Item_List)
    SupportRecyclerView mRecyclerView;

    protected CommandDataManager mCommandDataManager;

    protected CardAdapter<CommandData> mAdapter;

    /**
     * このFragmentが扱うカテゴリを取得する
     */
    protected abstract int getCommandCategory();

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        if (mCommandDataManager == null) {
            mCommandDataManager = new CommandDataManager(getContext());
        }

        if (mAdapter == null) {
            mAdapter = newCardAdapter();
            mRecyclerView.setAdapter(mAdapter, true);
        } else {
            mRecyclerView.setAdapter(mAdapter, true);
            mRecyclerView.setProgressVisibly(false, mAdapter.getCollection().size());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadDatabases();
    }

    @UiThread
    protected void loadDatabases() {
        asyncUI((BackgroundTask<CommandDataCollection> task) -> {
            return mCommandDataManager.loadFromCategory(getCommandCategory());
        }).completed((result, task) -> {
            onCommandLoaded(result);
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
    }

    @UiThread
    protected void onCommandLoaded(CommandDataCollection commands) {
        List<CommandData> list = commands.list(it -> true);
        mRecyclerView.setProgressVisibly(false, list.size());
        mAdapter.getCollection().insertOrReplaceAll(list);
        mAdapter.notifyDataSetChanged();
    }

    @UiThread
    protected void onClickCard(CommandData data) {
        MaterialAlertDialog dialog = newSettingDialog(data);
        addAutoDismiss(dialog).show();
    }

    /**
     * データを保存し、UI反映する
     */
    @UiThread
    protected void onCommitData(CommandData data) {
        mCommandDataManager.save(data);
        mAdapter.getCollection().insertOrReplace(data);
    }

    /**
     * 削除ダイアログを表示する
     */
    @UiThread
    protected void showDeleteDialog(CommandData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("コマンド削除");
        builder.setMessage("選択したコマンドを削除しますか?");
        builder.setPositiveButton("削除", (dlg, which) -> {
            mCommandDataManager.remove(data);
            mAdapter.getCollection().remove(data);
        });
        builder.setNegativeButton("キャンセル", null);
        builder.show();
    }

    /**
     * 表示用のAdapterを生成する
     */
    protected abstract CardAdapter<CommandData> newCardAdapter();

    /**
     * 設定Dialogを表示する
     */
    protected abstract MaterialAlertDialog newSettingDialog(CommandData data);
}
