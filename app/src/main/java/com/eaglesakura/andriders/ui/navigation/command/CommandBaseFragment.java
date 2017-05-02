package com.eaglesakura.andriders.ui.navigation.command;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.command.CommandDataCollection;
import com.eaglesakura.andriders.plugin.CommandDataManager;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.sloth.view.adapter.CardAdapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * コマンド設定のリスト表記を行う
 */
public abstract class CommandBaseFragment extends AppFragment {
    @Bind(R.id.Content_List)
    RecyclerView mRecyclerView;

    @Inject(AppManagerProvider.class)
    protected CommandDataManager mCommandDataManager;

    protected CardAdapter<CommandData> mAdapter;

    /**
     * このFragmentが扱うカテゴリを取得する
     */
    protected abstract int getCommandCategory();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (mAdapter == null) {
            mAdapter = newCardAdapter();
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.setAdapter(mAdapter);
//            mRecyclerView.setProgressVisibly(false, mAdapter.getCollection().size());
        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadCommands();
    }

    @UiThread
    protected void loadCommands() {
        asyncQueue((BackgroundTask<CommandDataCollection> task) -> {
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
//        mRecyclerView.setProgressVisibly(false, list.size());
        mAdapter.getCollection().insertOrReplaceAll(list);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 削除用の標準Listener
     */
    protected final CommandEditDialogBuilder.OnDeleteListener mCommandDeleteListener = data -> {
        mCommandDataManager.remove(data);
        mAdapter.getCollection().remove(data);
    };

    /**
     * データを保存し、UI反映する
     */
    @UiThread
    protected void onCommitData(CommandData data) {
        mCommandDataManager.save(data);
        mAdapter.getCollection().insertOrReplace(data);
    }

    /**
     * 表示用のAdapterを生成する
     */
    protected abstract CardAdapter<CommandData> newCardAdapter();
}
