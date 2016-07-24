package com.eaglesakura.andriders.ui.navigation.command.speed;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.databinding.CardCommandSpeedBinding;
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
import com.eaglesakura.android.margarine.BindStringArray;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.ui.spinner.BasicSpinnerAdapter;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.material.widget.MaterialAlertDialog;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.material.widget.support.SupportRecyclerView;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.List;

public class SpeedCommandFragment extends AppBaseFragment implements IFragmentPagerTitle {

    int mCategory;

    @Bind(R.id.Command_Item_List)
    SupportRecyclerView mRecyclerView;

    @BindStringArray(R.array.Command_Speed_TypeInfo)
    String[] mSpeedInfoFormats;

    CommandDataManager mCommandDataManager;

    public SpeedCommandFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_command_list);
        mCategory = CommandDatabase.CATEGORY_SPEED;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCommandDataManager = new CommandDataManager(context);
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
        mRecyclerView.setAdapter(mAdapter, true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadDatabases();
    }

    @OnClick(R.id.Command_Item_Add)
    void clickAddButton() {
        startActivityForResult(
                AppUtil.newCommandSettingIntent(getActivity(), CommandKey.fromSpeed(System.currentTimeMillis())),
                AppConstants.REQUEST_COMMAND_SETUP
        );
    }

    @UiThread
    void loadDatabases() {
        asyncUI((RxTask<CommandDataCollection> task) -> {
            return mCommandDataManager.loadFromCategory(mCategory);
        }).completed((result, task) -> {
            onCommandLoaded(result);
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
    }

    @UiThread
    void onCommandLoaded(CommandDataCollection commands) {
        List<CommandData> list = commands.list(it -> true);
        mRecyclerView.setProgressVisibly(false, list.size());
        mAdapter.getCollection().addAllAnimated(list);
    }

    CardAdapter<CommandData> mAdapter = new CardAdapter<CommandData>() {
        @Override
        protected View onCreateCard(ViewGroup parent, int viewType) {
            return CardCommandSpeedBinding.inflate(getActivity().getLayoutInflater(), null, false).getRoot();
        }

        @Override
        protected void onBindCard(CardBind<CommandData> bind, int position) {
            CommandData item = bind.getItem();
            CardCommandSpeedBinding binding = DataBindingUtil.getBinding(bind.getCard());

            binding.CommandItem.setOnClickListener(it -> {
                onClickCommand(item);
            });
            binding.setItem(new CardBinding() {
                @Override
                public Drawable getIcon() {
                    return new BitmapDrawable(getResources(), item.decodeIcon());
                }

                @Override
                public String getTitle() {
                    CommandData.RawExtra extra = item.getInternalExtra();
                    int type = extra.speedType;
                    double speed = extra.speedKmh;

                    return StringUtil.format(mSpeedInfoFormats[type], (int) speed);
                }
            });
        }
    };

    @UiThread
    void onClickCommand(CommandData data) {
        CommandData.RawExtra extra = data.getInternalExtra();
        final double SPEED_KMH = extra.speedKmh;
        final int TYPE = extra.speedType;

        MaterialAlertDialog dialog = new MaterialAlertDialog(getActivity()) {
            String[] mFooters;

            /**
             * 選択されているタイプ
             */
            int mSelectedType = TYPE;

            MaterialAlertDialog init() {
                setDialogContent(R.layout.dialog_speed_command);
                mFooters = getResources().getStringArray(R.array.Command_Speed_TypeFooter);

                AQuery q = new AQuery(root);
                {
                    String[] information = getResources().getStringArray(R.array.Command_Speed_TypeSelector);
                    BasicSpinnerAdapter adapter = new BasicSpinnerAdapter(getActivity());
                    for (String s : information) {
                        adapter.add(s);
                    }
                    q
                            .id(R.id.Command_Speed_Type)
                            .adapter(adapter)
                            .setSelection(TYPE)
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
                q.id(R.id.Command_Speed_Kmh).text(String.format("%d", (int) Math.max(SPEED_KMH, 0.0)));

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
                        case CommandData.SPEEDCOMMAND_TYPE_UPPER:
                        case CommandData.SPEEDCOMMAND_TYPE_LOWER:
                            toast("速度設定に間違いがあります");
                            return;
                    }
                }

                CommandData.RawExtra extra = data.getInternalExtra();
                extra.speedType = mSelectedType;
                extra.speedKmh = (float) speed;

                mCommandDataManager.save(data);

                // UIを更新させる
                mAdapter.getCollection().insertOrReplace(data);
            }
        }.init();

        addAutoDismiss(dialog).show();
    }

    @UiThread
    void showDeleteDialog(CommandData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("コマンド削除");
        builder.setMessage("選択したコマンドを削除しますか?");
        builder.setPositiveButton("削除", (dlg, which) -> {
            mCommandDataManager.remove(data);
            mAdapter.getCollection().remove(data);
        });
        builder.setNegativeButton("キャンセル", null);
        builder.show();
    }

    @OnActivityResult(AppConstants.REQUEST_COMMAND_SETUP)
    void resultCommandSetup(int result, Intent intent) {
        CommandSetupData data = CommandSetupData.getFromResult(intent);
        if (data == null) {
            return;
        }

        CommandData.RawExtra extra = new CommandData.RawExtra();
        extra.speedKmh = 25.0f;
        extra.speedType = CommandData.SPEEDCOMMAND_TYPE_UPPER;
        CommandData commandData = mCommandDataManager.save(data, mCategory, extra);
        mAdapter.getCollection().insertOrReplace(0, commandData);
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
