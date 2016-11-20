package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.data.display.DisplayLayoutManager;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.model.display.DisplayLayoutGroup;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.android.util.ViewUtil;
import com.squareup.otto.Subscribe;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 実際のディスプレイレイアウト変更を行うFragment
 */
public class LayoutEditFragment extends AppFragment {

    DisplayLayoutController mDisplayLayoutController;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = DisplayLayoutManager.newStubLayout(inflater.getContext());
        return view;
    }

    /**
     * アプリが切り替えられた
     */
    @Subscribe
    void onSelectedApp(DisplayLayoutController.Bus bus) {
        mDisplayLayoutController = bus.getData();
        DisplayLayoutApplication selectedApp = mDisplayLayoutController.getSelectedApp();
        DisplayLayoutGroup layoutGroup = mDisplayLayoutController.getLayoutGroup(selectedApp.getPackageName());

        // レイアウトを更新する
        for (DisplayLayout layout : layoutGroup.list()) {
            updateLayout(layout);
        }
    }

    @UiThread
    void updateLayout(DisplayLayout layout) {

//        ViewGroup stub = (ViewGroup) getView().findViewById(layout.getSlotId());
//        ViewGroup stub = (ViewGroup) getView().findViewWithTag(layout.getSlotId());
        ViewGroup stub = ViewUtil.findViewByMatcher(getView(), view -> Integer.valueOf(layout.getSlotId()).equals(view.getTag(R.id.Tag_SlotId)));

        // 一旦個をすべて削除する
        stub.removeAllViews();

        // ボタンを生成する
        AppCompatButton button = new AppCompatButton(getContext());
        // キーを検索する
        DisplayKey information = mDisplayLayoutController.getDisplayKey(layout);
        if (information == null) {
            if (layout.hasValue()) {
                // 情報を見失った
                button.setText(R.string.Word_Display_ErrorSlot);
            }
        } else {
            button.setText(information.getTitle());
        }


        stub.addView(button);
    }

}
