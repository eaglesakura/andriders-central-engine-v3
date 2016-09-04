package com.eaglesakura.andriders.ui.navigation.command.distance;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.databinding.CardCommandDistanceBinding;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.db.command.CommandDatabase;
import com.eaglesakura.andriders.db.command.CommandSetupData;
import com.eaglesakura.andriders.ui.navigation.command.CommandBaseFragment;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.IFragmentPagerTitle;
import com.eaglesakura.android.margarine.BindStringArray;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.material.widget.MaterialAlertDialog;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.util.MathUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

/**
 * タイマーコマンドのセットアップ
 */
public class DistanceCommandFragment extends CommandBaseFragment implements IFragmentPagerTitle {
    final int REQUEST_COMMAND_SETUP = AppConstants.REQUEST_COMMAND_SETUP_DISTANCE;

    @BindStringArray(R.array.Command_Distance_TypeInfo)
    protected String[] mInfoFormats;

    public DistanceCommandFragment() {
        mFragmentDelegate.setLayoutId(R.layout.command_setup_list);
    }

    @Override
    protected int getCommandCategory() {
        return CommandDatabase.CATEGORY_DISTANCE;
    }

    @Override
    protected CardAdapter<CommandData> newCardAdapter() {
        return new CardAdapter<CommandData>() {
            @Override
            protected View onCreateCard(ViewGroup parent, int viewType) {
                return CardCommandDistanceBinding.inflate(getActivity().getLayoutInflater(), parent, false).getRoot();
            }

            @Override
            protected void onBindCard(CardBind<CommandData> bind, int position) {
                CommandData item = bind.getItem();
                CardCommandDistanceBinding binding = DataBindingUtil.getBinding(bind.getCard());
                binding.setItem(new CardBinding() {
                    @Override
                    public Drawable getIcon() {
                        return new BitmapDrawable(getResources(), item.decodeIcon());
                    }

                    @Override
                    public String getTitle() {
                        CommandData.RawExtra extra = item.getInternalExtra();
                        String text = StringUtil.format(
                                mInfoFormats[extra.distanceType],
                                extra.distanceKm
                        );
                        if ((extra.flags & CommandData.DISTANCE_FLAG_REPEAT) != 0) {
                            text += (" / " + getString(R.string.Command_Flag_Repeat));
                        }
                        if ((extra.flags & CommandData.DISTANCE_FLAG_ACTIVE_ONLY) != 0) {
                            text += (" / " + getString(R.string.Command_Flag_Distance_ActiveOnly));
                        }
                        return text;
                    }
                });
                binding.CommandItem.setOnClickListener(it -> {
                    addAutoDismiss(newSettingDialog(item)).show();
                });
            }
        };
    }

    @OnClick(R.id.Command_Item_Add)
    protected void clickAddButton() {
        startActivityForResult(
                AppUtil.newCommandSettingIntent(getActivity(), CommandKey.fromTimer(System.currentTimeMillis())),
                REQUEST_COMMAND_SETUP
        );
    }

    @OnActivityResult(REQUEST_COMMAND_SETUP)
    void resultCommandSetup(int result, Intent intent) {
        CommandSetupData data = CommandSetupData.getFromResult(intent);
        if (data == null) {
            return;
        }

        CommandData.RawExtra extra = new CommandData.RawExtra();
        extra.distanceKm = 5.0f;
        extra.distanceType = CommandData.DISTANCE_TYPE_SESSION;
        CommandData commandData = mCommandDataManager.save(data, getCommandCategory(), extra);
        mAdapter.getCollection().insertOrReplace(0, commandData);
    }

    @Override
    protected MaterialAlertDialog newSettingDialog(CommandData data) {
        return new MaterialAlertDialog(getActivity()) {
            MaterialAlertDialog init() {
                setDialogContent(R.layout.command_setup_distance_dialog);

                initTimerUi();

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
            void initTimerUi() {
                CommandData.RawExtra extra = data.getInternalExtra();
                new AQuery(root)
                        .id(R.id.Command_Distance_Type).setSelection(data.getInternalExtra().distanceType)
                        .id(R.id.Command_Distance_Text).text(StringUtil.format("%f", extra.distanceKm))
                        .id(R.id.Command_Distance_Repeat).checked((extra.flags & CommandData.DISTANCE_FLAG_REPEAT) != 0)
                        .id(R.id.Command_Distance_ActiveOnly).checked((extra.flags & CommandData.DISTANCE_FLAG_ACTIVE_ONLY) != 0)
                ;
            }

            /**
             * 変更を確定する
             */
            void onCommit() {
                AQuery q = new AQuery(root);
                CommandData.RawExtra extra = data.getInternalExtra();
                float interval = (float) ViewUtil.getDoubleValue(q.id(R.id.Command_Distance_Text).getEditText(), -1);
                if (interval <= 0) {
                    toast("距離設定に間違いがあります");
                    return;
                }

                extra.distanceType = q.id(R.id.Command_Distance_Type).getSelectedItemPosition();
                extra.distanceKm = interval;
                extra.flags = MathUtil.setFlag(extra.flags, CommandData.DISTANCE_FLAG_REPEAT, q.id(R.id.Command_Distance_Repeat).isChecked());
                extra.flags = MathUtil.setFlag(extra.flags, CommandData.DISTANCE_FLAG_ACTIVE_ONLY, q.id(R.id.Command_Distance_ActiveOnly).isChecked());

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
        return "距離コマンド";
    }
}
