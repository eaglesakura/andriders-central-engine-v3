package com.eaglesakura.andriders.ui.navigation.command.speed;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.databinding.CommandSetupSpeedRowBinding;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.command.CommandDatabase;
import com.eaglesakura.andriders.model.command.CommandSetupData;
import com.eaglesakura.andriders.ui.navigation.command.CommandBaseFragment;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.IFragmentPagerTitle;
import com.eaglesakura.android.margarine.BindStringArray;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.ui.spinner.BasicSpinnerAdapter;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.material.widget.MaterialAlertDialog;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.util.StringUtil;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

public class SpeedCommandFragment extends CommandBaseFragment implements IFragmentPagerTitle {

    final int REQUEST_COMMAND_SETUP = AppConstants.REQUEST_COMMAND_SETUP_SPEED;

    @BindStringArray(R.array.Command_Speed_TypeInfo)
    protected String[] mInfoFormats;

    public SpeedCommandFragment() {
        mFragmentDelegate.setLayoutId(R.layout.command_setup_list);
    }

    @Override
    protected int getCommandCategory() {
        return CommandDatabase.CATEGORY_SPEED;
    }

    /**
     * リスト表示用Adapterを生成する
     */
    @Override
    protected CardAdapter<CommandData> newCardAdapter() {
        return new CardAdapter<CommandData>() {
            @Override
            protected View onCreateCard(ViewGroup parent, int viewType) {
                return CommandSetupSpeedRowBinding.inflate(getActivity().getLayoutInflater(), null, false).getRoot();
            }

            @Override
            protected void onBindCard(CardBind<CommandData> bind, int position) {
                CommandData item = bind.getItem();
                CommandSetupSpeedRowBinding binding = DataBindingUtil.getBinding(bind.getCard());

                binding.CommandItem.setOnClickListener(it -> {
                    onClickCard(item);
                });
                binding.setItem(new CardBinding() {
                    @Override
                    public Drawable getIcon() {
                        return new BitmapDrawable(getResources(), item.decodeIcon());
                    }

                    @Override
                    public String getTitle() {
                        CommandData.Extra extra = item.getInternalExtra();
                        int type = extra.speedType;
                        double speed = extra.speedKmh;

                        return StringUtil.format(mInfoFormats[type], (int) speed);
                    }
                });
            }
        };
    }

    @OnClick(R.id.Command_Item_Add)
    protected void clickAddButton() {
        startActivityForResult(
                AppUtil.newCommandSettingIntent(getActivity(), CommandKey.fromSpeed(System.currentTimeMillis())),
                REQUEST_COMMAND_SETUP
        );
    }

    @OnActivityResult(REQUEST_COMMAND_SETUP)
    void resultCommandSetup(int result, Intent intent) {
        CommandSetupData data = CommandSetupData.getFromResult(intent);
        if (data == null) {
            return;
        }

        CommandData.Extra extra = new CommandData.Extra();
        extra.speedKmh = 25.0f;
        extra.speedType = CommandData.SPEED_TYPE_UPPER;
        CommandData commandData = mCommandDataManager.save(data, getCommandCategory(), extra);
        mAdapter.getCollection().insertOrReplace(0, commandData);
    }

    /**
     * 設定用のDialogを開く
     *
     * @param data 設定対象のデータ
     */
    @Override
    protected MaterialAlertDialog newSettingDialog(CommandData data) {
        return new MaterialAlertDialog(getActivity()) {
            String[] mFooters;

            /**
             * 選択されているタイプ
             */
            int mSelectedType = data.getInternalExtra().speedType;

            MaterialAlertDialog init() {
                setDialogContent(R.layout.command_setup_speed_dialog);
                mFooters = getResources().getStringArray(R.array.Command_Speed_TypeFooter);

                initTypeSelector();
                initSpeedInput();

                setTitle("条件変更");
                setPositiveButton("保存", (dlg, which) -> {
                    onCommit();
                });

                setNeutralButton("削除", (dlg, which) -> {
                    showDeleteDialog(data);
                });
                return this;
            }


            /**
             * type設定のUIを構築する
             */
            void initTypeSelector() {
                String[] information = getResources().getStringArray(R.array.Command_Speed_TypeSelector);
                BasicSpinnerAdapter adapter = new BasicSpinnerAdapter(getContext());
                for (String s : information) {
                    adapter.add(s);
                }
                new AQuery(root).id(R.id.Command_Speed_Type)
                        .adapter(adapter)
                        .setSelection(mSelectedType)
                        .itemSelected(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                onTypeSelected(position);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
            }

            void initSpeedInput() {
                new AQuery(root).id(R.id.Command_Speed_Kmh)
                        .text(String.format("%d", (int) Math.max(data.getInternalExtra().speedKmh, 0.0)));
            }

            /**
             *
             */
            void onTypeSelected(int position) {
                mSelectedType = position;
                AQuery q = new AQuery(root);
                String footer = mFooters[mSelectedType];
                if (StringUtil.isEmpty(footer)) {
                    q.id(R.id.Setting_SpeedCommand_EditRoot).gone();
                } else {
                    q.id(R.id.Setting_SpeedCommand_EditRoot).visible()
                            .id(R.id.Command_Speed_Info).text(footer);
                }
            }

            /**
             * 変更を確定する
             */
            void onCommit() {
                AQuery q = new AQuery(root);
                double speed = ViewUtil.getDoubleValue(q.id(R.id.Command_Speed_Kmh).getEditText(), -1);
                if (speed < 0) {
                    switch (mSelectedType) {
                        case CommandData.SPEED_TYPE_UPPER:
                        case CommandData.SPEED_TYPE_LOWER:
                            toast("速度設定に間違いがあります");
                            return;
                    }
                }

                CommandData.Extra extra = data.getInternalExtra();
                extra.speedType = mSelectedType;
                extra.speedKmh = (float) speed;

                onCommitData(data);
            }
        }.init();
    }

    public interface CardBinding {
        Drawable getIcon();

        String getTitle();
    }

    @Override
    public CharSequence getTitle() {
        return "スピード";
    }
}
