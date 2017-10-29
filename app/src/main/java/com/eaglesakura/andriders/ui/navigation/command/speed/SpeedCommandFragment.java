package com.eaglesakura.andriders.ui.navigation.command.speed;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.databinding.CommandSetupSpeedRowBinding;
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
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

@FragmentLayout(R.layout.command_setup_list)
public class SpeedCommandFragment extends CommandBaseFragment implements FragmentPagerTitle {

    final int REQUEST_COMMAND_SETUP = AppConstants.REQUEST_COMMAND_SETUP_SPEED;

    @BindStringArray(R.array.Message_Command_Speed)
    protected String[] mInfoFormats;

    @Override
    protected int getCommandCategory() {
        return CommandData.CATEGORY_SPEED;
    }

    /**
     * リスト表示用Adapterを生成する
     */
    @Override
    protected CardAdapter<CommandData> newCardAdapter() {
        return new CardAdapter<CommandData>() {
            @Override
            protected View onCreateCard(ViewGroup parent, int viewType) {
                return CommandSetupSpeedRowBinding.inflate(getActivity().getLayoutInflater(), parent, false).getRoot();
            }

            @Override
            protected void onBindCard(CardBind<CommandData> bind, int position) {
                CommandData item = bind.getItem();
                CommandSetupSpeedRowBinding binding = DataBindingUtil.getBinding(bind.getCard());

                binding.Item.setOnClickListener(it -> {
                    CommandEditDialogBuilder.from(getContext(), item)
                            .commit(mCommandCommitListener)
                            .delete(mCommandDeleteListener)
                            .show(getFragmentLifecycle());
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

    @OnClick(R.id.Button_Add)
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
     * 編集確定時の動作を行う
     */
    CommandEditDialogBuilder.OnCommitListener mCommandCommitListener = (view, data) -> {
        AQuery q = new AQuery(view);
        int selectedType = q.id(R.id.Selector_Type).getSelectedItemPosition();
        double speed = ViewUtil.getDoubleValue(q.id(R.id.Item_Value).getEditText(), -1);
        if (speed < 0) {
            switch (selectedType) {
                case CommandData.SPEED_TYPE_UPPER:
                case CommandData.SPEED_TYPE_LOWER:
                    SnackbarBuilder.from(this).message(R.string.Message_Command_InvalidValue).show();
                    return;
            }
        }

        CommandData.Extra extra = data.getInternalExtra();
        extra.speedType = selectedType;
        extra.speedKmh = (float) speed;

        onCommitData(data);
    };

    public interface CardBinding {
        Drawable getIcon();

        String getTitle();
    }

    @Override
    public CharSequence getTitle(Context context) {
        return context.getString(R.string.Title_Command_Speed);
    }
}
