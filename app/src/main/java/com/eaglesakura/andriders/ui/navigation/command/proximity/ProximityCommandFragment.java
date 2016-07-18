package com.eaglesakura.andriders.ui.navigation.command.proximity;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.db.AppSettings;
import com.eaglesakura.andriders.db.command.CommandDataCollection;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.db.command.CommandDatabase;
import com.eaglesakura.andriders.db.command.CommandSetupData;
import com.eaglesakura.andriders.plugin.CommandDataManager;
import com.eaglesakura.andriders.provider.StorageProvider;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.IFragmentPagerTitle;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.android.util.ResourceUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.UiThread;
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

    @Inject(StorageProvider.class)
    AppSettings mAppSettings;

    /**
     * 最後に選択した近接コマンドボタン
     */
    @BundleState
    int mLastSelectedProximity;

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
        mLinkDisplaySwitch.setChecked(mSettings.getCentralSettings().getProximityCommandScreenLink());
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
                        mLastSelectedProximity = PROXIMITY_INDEX;
                        startActivityForResult(
                                AppUtil.newCommandSettingIntent(getActivity(), KEY),
                                AppConstants.REQUEST_COMMAND_SETUP
                        );
                    })
                    .id(R.id.Setting_Command_ProximitySec).text(StringUtil.format("%d 秒", index + 1))
                    .id(R.id.Setting_Command_Icon).ifPresent(ImageView.class,
                    it -> {
                        if (data != null) {
                            it.setImageBitmap(data.decodeIcon());
                        } else {
                            it.setImageDrawable(ResourceUtil.drawable(getActivity(), R.mipmap.ic_delete_grey));
                        }
                    });
            ++index;
        }
    }

    @OnActivityResult(AppConstants.REQUEST_COMMAND_SETUP)
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
