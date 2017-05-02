package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.ui.widget.IconItemAdapter;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.sloth.annotation.BindInterface;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;
import com.eaglesakura.sloth.ui.progress.ProgressToken;
import com.squareup.otto.Subscribe;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.UiThread;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

/**
 * レイアウト対象のアプリを選択するFragment
 */
@FragmentLayout(R.layout.display_setup_appselect)
public class LayoutAppSelectFragment extends AppFragment {

    @BindInterface
    Callback mCallback;

    DisplayLayoutController mDisplayLayoutController;

    @OnClick(R.id.Button_AppSelect)
    void clickAppSelect() {
        asyncQueue(task -> {
            SupportCancelCallbackBuilder.CancelChecker checker = SupportCancelCallbackBuilder.from(task).build();
            try (ProgressToken token = pushProgress(R.string.Word_Common_DataLoad)) {
                mDisplayLayoutController.loadTargetApplications(checker);
                return this;
            }
        }).completed((result, task) -> {
            onLoadApplicationList();
        }).failed((error, task) -> {
            AppLog.printStackTrace(error);
            AppDialogBuilder.newError(getContext(), error)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getLifecycle());
        }).start();

    }

    /**
     * アプリ一覧のロードが完了した
     */
    @UiThread
    void onLoadApplicationList() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.display_setup_appselect_dialog, null, false);
        Dialog dialog = AppDialogBuilder.newCustomContent(getContext(), getString(R.string.Title_Command_ChooseApp), view)
                .fullScreen(true)
                .show(getLifecycle());

        RecyclerView supportRecyclerView = ViewUtil.findViewByMatcher(view, it -> (it instanceof RecyclerView));
        IconItemAdapter<DisplayLayoutApplication> adapter = new IconItemAdapter<DisplayLayoutApplication>(getLifecycle()) {
            @Override
            protected Context getContext() {
                return getActivity();
            }

            @Override
            protected void onItemSelected(int position, DisplayLayoutApplication item) {
                dialog.dismiss();
                mCallback.onApplicationSelected(LayoutAppSelectFragment.this, item);
            }
        };
        adapter.getCollection().addAll(mDisplayLayoutController.listSortedApplications());

        supportRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        supportRecyclerView.setAdapter(adapter);
    }

    /**
     * アプリが切り替えられた
     */
    @Subscribe
    void onSelectedApp(DisplayLayoutController.Bus bus) {
        mDisplayLayoutController = bus.getData();

        DisplayLayoutApplication data = mDisplayLayoutController.getSelectedApp();
        new AQuery(getView())
                .id(R.id.Item_Title).text(data.getTitle())  // タイトル設定
                .id(R.id.Item_Icon).image(data.getIcon())   // アイコン設定
                .id(R.id.Button_Delete).visibility(data.isDefaultApp() ? View.GONE : View.VISIBLE).clicked(view -> mCallback.onRequestDeleteLayout(this, data));    // 削除ボタン設定
    }

    public interface Callback {
        /**
         * 表示対象のアプリが選択された
         */
        void onApplicationSelected(LayoutAppSelectFragment fragment, DisplayLayoutApplication selected);

        /**
         * 削除がリクエストされた
         *
         * @param packageName 削除対象のパッケージ名
         */
        void onRequestDeleteLayout(LayoutAppSelectFragment fragment, DisplayLayoutApplication packageName);
    }
}
