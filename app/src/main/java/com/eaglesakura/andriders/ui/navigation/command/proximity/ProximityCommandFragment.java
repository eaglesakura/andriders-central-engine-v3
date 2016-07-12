package com.eaglesakura.andriders.ui.navigation.command.proximity;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.db.AppSettings;
import com.eaglesakura.andriders.provider.StorageProvider;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.IFragmentPagerTitle;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;

import android.support.annotation.UiThread;
import android.view.ViewGroup;
import android.widget.CompoundButton;

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


    public ProximityCommandFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_command_proximity);
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

        for (ViewGroup cmdView : mCommandViewList) {
            updateProximityUI(cmdView);
        }
    }

    /**
     * 近接コマンドのUIを切り替える
     */
    @UiThread
    void updateProximityUI(ViewGroup root) {

    }

    @Override
    public CharSequence getTitle() {
        return "近接";
    }
}
