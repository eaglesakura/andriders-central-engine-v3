package com.eaglesakura.andriders.ui.navigation.command.timer;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.databinding.CommandSetupTimerRowBinding;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.command.CommandSetupData;
import com.eaglesakura.andriders.ui.navigation.command.CommandBaseFragment;
import com.eaglesakura.andriders.ui.navigation.command.CommandEditDialogBuilder;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.margarine.BindStringArray;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.ui.pager.FragmentPagerTitle;
import com.eaglesakura.sloth.view.adapter.CardAdapter;
import com.eaglesakura.sloth.view.builder.SnackbarBuilder;
import com.eaglesakura.util.MathUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

/**
 * タイマーコマンドのセットアップ
 */
@FragmentLayout(R.layout.command_setup_list)
public class TimerCommandFragment extends CommandBaseFragment implements FragmentPagerTitle {
    final int REQUEST_COMMAND_SETUP = AppConstants.REQUEST_COMMAND_SETUP_TIMER;

    @BindStringArray(R.array.Message_Command_Timer)
    protected String[] mInfoFormats;

    @Override
    protected int getCommandCategory() {
        return CommandData.CATEGORY_TIMER;
    }

    @Override
    protected CardAdapter<CommandData> newCardAdapter() {
        return new CardAdapter<CommandData>() {
            @Override
            protected View onCreateCard(ViewGroup parent, int viewType) {
                return CommandSetupTimerRowBinding.inflate(getActivity().getLayoutInflater(), parent, false).getRoot();
            }

            @Override
            protected void onBindCard(CardBind<CommandData> bind, int position) {
                CommandData item = bind.getItem();
                CommandSetupTimerRowBinding binding = DataBindingUtil.getBinding(bind.getCard());
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
                        if ((item.getInternalExtra().flags & CommandData.TIMER_FLAG_REPEAT) != 0) {
                            text += (" / " + getString(R.string.Word_Common_Repeat));
                        }
                        return text;
                    }
                });
                binding.Item.setOnClickListener(it -> {
                    CommandEditDialogBuilder.from(getContext(), item)
                            .commit(mCommandCommitListener)
                            .delete(mCommandDeleteListener)
                            .show(getLifecycle());
                });
            }
        };
    }

    @OnClick(R.id.Button_Add)
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

        CommandData.Extra extra = new CommandData.Extra();
        extra.timerIntervalSec = (60 * 5);
        extra.timerType = CommandData.TIMER_TYPE_SESSION;
        CommandData commandData = mCommandDataManager.save(data, getCommandCategory(), extra);
        mAdapter.getCollection().insertOrReplace(0, commandData);
    }

    CommandEditDialogBuilder.OnCommitListener mCommandCommitListener = (view, data) -> {
        AQuery q = new AQuery(view);
        CommandData.Extra extra = data.getInternalExtra();
        int interval = (int) ViewUtil.getLongValue(q.id(R.id.Item_Value).getEditText(), -1);
        if (interval < 0) {
            SnackbarBuilder.from(this).message(R.string.Message_Command_InvalidValue).show();
            return;
        }

        extra.timerType = q.id(R.id.Selector_Type).getSelectedItemPosition();
        extra.timerIntervalSec = interval;
        extra.flags = MathUtil.setFlag(extra.flags, CommandData.TIMER_FLAG_REPEAT, q.id(R.id.Button_Repeat).isChecked());

        onCommitData(data);
    };

    public interface CardBinding {
        Drawable getIcon();

        String getTitle();
    }

    @Override
    public CharSequence getTitle(Context context) {
        return context.getString(R.string.Title_Command_Timer);
    }
}
