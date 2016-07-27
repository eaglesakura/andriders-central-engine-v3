package com.eaglesakura.andriders.ui.navigation.command.proximity;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.db.command.CommandDataCollection;
import com.eaglesakura.andriders.db.command.CommandDatabase;
import com.eaglesakura.andriders.db.command.CommandSetupData;
import com.eaglesakura.andriders.plugin.CommandDataManager;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.IFragmentPagerTitle;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.util.ResourceUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 近接コマンド設定クラス
 */
public class ProximityCommandFragment extends AppBaseFragment implements IFragmentPagerTitle {

    @Bind(R.id.Command_Proximity_DisplayLink)
    CompoundButton mLinkDisplaySwitch;

    /**
     * 近接コマンドボタン
     */
    final List<ViewGroup> mCommandViewList = new ArrayList<>();

    CommandDataManager mCommandManager;

    public ProximityCommandFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_command_proximity);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCommandManager = new CommandDataManager(context);
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        // ディスプレイリンクの設定
        mLinkDisplaySwitch.setChecked(mSettings.getCentralSettings().isProximityCommandScreenLink());
        mLinkDisplaySwitch.setOnCheckedChangeListener((it, checked) -> {
            mLinkDisplaySwitch.setChecked(checked);
            mSettings.getCentralSettings().setProximityCommandScreenLink(checked);
            mSettings.getCentralSettings().commit();
        });

        AQuery q = new AQuery(self.getView());
        mCommandViewList.add(q.id(R.id.Command_Proximity_Index0).getView(ViewGroup.class));
        mCommandViewList.add(q.id(R.id.Command_Proximity_Index1).getView(ViewGroup.class));
        mCommandViewList.add(q.id(R.id.Command_Proximity_Index2).getView(ViewGroup.class));
        mCommandViewList.add(q.id(R.id.Command_Proximity_Index3).getView(ViewGroup.class));
        updateProximityUI();
    }

    /**
     * 近接コマンドのUIを切り替える
     */
    @UiThread
    void updateProximityUI() {

        CommandDataCollection collection = mCommandManager.loadFromCategory(CommandDatabase.CATEGORY_PROXIMITY);


        int index = 0;
        for (ViewGroup cmdView : mCommandViewList) {
            final int PROXIMITY_INDEX = index;
            final CommandKey KEY = CommandKey.fromProximity(PROXIMITY_INDEX + 1);
            CommandData data = collection.getOrNull(KEY);
            new AQuery(cmdView)
                    .clicked(it -> {
                        if (data == null) {
                            startCommandSetup(KEY);
                        } else {
                            bootDeleteCheckDialog(data);
                        }
                    })
                    .id(R.id.Setting_Command_ProximitySec).text(StringUtil.format("%d 秒", index + 1))
                    .id(R.id.Setting_Command_Icon).ifPresent(ImageView.class,
                    it -> {
                        if (data != null) {
                            it.setImageBitmap(data.decodeIcon());
                        } else {
                            it.setImageDrawable(ResourceUtil.drawable(getActivity(), R.mipmap.ic_common_none));
                        }
                    });
            ++index;
        }
    }

    @UiThread
    void bootDeleteCheckDialog(CommandData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("確認");
        builder.setTitle("既にコマンドが存在します。\n古いコマンドを削除しますか？");
        builder.setPositiveButton("削除", (dlg, which) -> {
            mCommandManager.remove(data);
            updateProximityUI();
        });
        builder.setNeutralButton("上書", (dlg, which) -> {
            startCommandSetup(data.getKey());
        });
        builder.show();
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

        AppLog.plugin("key[%s] package[%s]", setupData.getKey().getKey(), setupData.getPackageName());

        // DBに保存を行わせ、更新する
        mCommandManager.save(setupData, CommandDatabase.CATEGORY_PROXIMITY, null);
        updateProximityUI();
    }


    @Override
    public CharSequence getTitle() {
        return "近接";
    }
}
