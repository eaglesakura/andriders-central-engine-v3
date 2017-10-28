package com.eaglesakura.andriders.ui.navigation.command.proximity;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.command.CommandDataCollection;
import com.eaglesakura.andriders.model.command.CommandSetupData;
import com.eaglesakura.andriders.plugin.CommandDataManager;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.util.DrawableUtil;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.ui.pager.FragmentPagerTitle;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

/**
 * 近接コマンド設定クラス
 */
@FragmentLayout(R.layout.command_setup_proximity)
public class ProximityCommandFragment extends AppFragment implements FragmentPagerTitle {

    @Bind(R.id.Command_Proximity_DisplayLink)
    CompoundButton mLinkDisplaySwitch;

    /**
     * 近接コマンドボタン
     */
    final List<ViewGroup> mCommandViewList = new ArrayList<>();

    @Inject(AppManagerProvider.class)
    CommandDataManager mCommandManager;

    @Inject(AppContextProvider.class)
    AppSettings mSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // ディスプレイリンクの設定
        mLinkDisplaySwitch.setChecked(mSettings.getCentralSettings().isProximityCommandScreenLink());
        mLinkDisplaySwitch.setOnCheckedChangeListener((it, checked) -> {
            mLinkDisplaySwitch.setChecked(checked);
            mSettings.getCentralSettings().setProximityCommandScreenLink(checked);
            mSettings.getCentralSettings().commit();
        });

        AQuery q = new AQuery(view);
        mCommandViewList.add(q.id(R.id.Command_Proximity_Index0).getView(ViewGroup.class));
        mCommandViewList.add(q.id(R.id.Command_Proximity_Index1).getView(ViewGroup.class));
        mCommandViewList.add(q.id(R.id.Command_Proximity_Index2).getView(ViewGroup.class));
        mCommandViewList.add(q.id(R.id.Command_Proximity_Index3).getView(ViewGroup.class));
        updateProximityUI();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCommandViewList.clear();
    }

    /**
     * 近接コマンドのUIを切り替える
     *
     * 近接コマンドは最大4つなので、同期的に読み込んでしまう。
     */
    @UiThread
    void updateProximityUI() {
        CommandDataCollection collection = mCommandManager.loadFromCategory(CommandData.CATEGORY_PROXIMITY);

        int index = 0;
        for (ViewGroup cmdView : mCommandViewList) {
            final int PROXIMITY_INDEX = index;
            final CommandKey KEY = CommandKey.fromProximity(PROXIMITY_INDEX + 1);
            CommandData data = collection.find(KEY);
            new AQuery(cmdView)
                    .clicked(it -> {
                        if (data == null) {
                            startCommandSetup(KEY);
                        } else {
                            bootDeleteCheckDialog(data);
                        }
                    })
                    .id(R.id.Setting_Command_ProximitySec).text(StringUtil.format("%d 秒", index + 1))
                    .id(R.id.Setting_Command_Icon).ifPresent(AppCompatImageView.class,
                    it -> {
                        if (data != null) {
                            it.setImageBitmap(data.decodeIcon());
                        } else {
                            it.setImageDrawable(DrawableUtil.getVectorDrawable(getContext(), R.drawable.ic_common_none, R.color.App_Icon_Grey));
                        }
                    });
            ++index;
        }
    }

    @UiThread
    void bootDeleteCheckDialog(CommandData data) {
        AppDialogBuilder.newInformation(getContext(), "既にコマンドが存在します。\n古いコマンドを削除しますか？")
                .positiveButton("削除", () -> {
                    mCommandManager.remove(data);
                    updateProximityUI();
                })
                .negativeButton("上書", () -> {
                    startCommandSetup(data.getKey());
                })
                .show(getFragmentLifecycle());
    }

    @UiThread
    void startCommandSetup(CommandKey key) {
        startActivityForResult(
                AppUtil.newCommandSettingIntent(getActivity(), key),
                AppConstants.REQUEST_COMMAND_SETUP_PROXIMITY
        );
    }

    @OnActivityResult(AppConstants.REQUEST_COMMAND_SETUP_PROXIMITY)
    void resultCommandSetup(int result, Intent data) {
        CommandSetupData setupData = CommandSetupData.getFromResult(data);
        if (setupData == null) {
            return;
        }

        AppLog.plugin("key[%s] package[%s]", setupData.getKey().toString(), setupData.getPackageName());

        // DBに保存を行わせ、更新する
        mCommandManager.save(setupData, CommandData.CATEGORY_PROXIMITY, null);
        updateProximityUI();
    }

    @Override
    public CharSequence getTitle(Context context) {
        return context.getString(R.string.Title_Command_Proximity);
    }
}
