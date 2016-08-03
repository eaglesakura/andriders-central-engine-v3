package com.eaglesakura.andriders.ui.navigation.command.timer;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.databinding.CardCommandTimerBinding;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.db.command.CommandDatabase;
import com.eaglesakura.andriders.db.command.CommandSetupData;
import com.eaglesakura.andriders.ui.navigation.command.CommandBaseFragment;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.framework.delegate.fragment.IFragmentPagerTitle;
import com.eaglesakura.android.margarine.BindStringArray;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.material.widget.MaterialAlertDialog;
import com.eaglesakura.material.widget.adapter.CardAdapter;
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
public class TimerCommandFragment extends CommandBaseFragment implements IFragmentPagerTitle {
    final int REQUEST_COMMAND_SETUP = AppConstants.REQUEST_COMMAND_SETUP_TIMER;

    @BindStringArray(R.array.Command_Timer_TypeInfo)
    protected String[] mInfoFormats;

    public TimerCommandFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_command_list);
    }

    @Override
    protected int getCommandCategory() {
        return CommandDatabase.CATEGORY_TIMER;
    }

    @Override
    protected CardAdapter<CommandData> newCardAdapter() {
        return new CardAdapter<CommandData>() {
            @Override
            protected View onCreateCard(ViewGroup parent, int viewType) {
                return CardCommandTimerBinding.inflate(getActivity().getLayoutInflater(), parent, false).getRoot();
            }

            @Override
            protected void onBindCard(CardBind<CommandData> bind, int position) {
                CommandData item = bind.getItem();
                CardCommandTimerBinding binding = DataBindingUtil.getBinding(bind.getCard());
                binding.setItem(new CardBinding() {
                    @Override
                    public Drawable getIcon() {
                        return new BitmapDrawable(getResources(), item.decodeIcon());
                    }

                    @Override
                    public String getTitle() {
                        String text = StringUtil.format(
                                mInfoFormats[item.getInternalExtra().timerType],
                                AppUtil.formatTimeMilliSecToString(item.getInternalExtra().timerIntervalSec * 1000)
                        );
                        if ((item.getInternalExtra().flags & CommandData.TIMERCOMMAND_FLAG_REPEAT) != 0) {
                            text = ("/" + getString(R.string.Command_Timer_Repeat));
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
        extra.timerIntervalSec = (60 * 5);
        extra.timerType = CommandData.TIMERCOMMAND_TYPE_SESSION;
        CommandData commandData = mCommandDataManager.save(data, getCommandCategory(), extra);
        mAdapter.getCollection().insertOrReplace(0, commandData);
    }

    @Override
    protected MaterialAlertDialog newSettingDialog(CommandData data) {
        return null;
    }

    public interface CardBinding {
        Drawable getIcon();

        String getTitle();
    }

    @Override
    public CharSequence getTitle() {
        return "タイマー";
    }
}
