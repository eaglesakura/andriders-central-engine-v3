package com.eaglesakura.andriders.ui.navigation.info;

import com.eaglesakura.andriders.AceApplication;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import java.util.concurrent.Executor;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * アプリ情報画面を管理する。
 * <p/>
 * * Central設定
 * <p/>
 * * ビルド情報
 */
public class InformationFragmentMain extends BaseNavigationFragment {

    public InformationFragmentMain() {
        requestInjection(R.layout.fragment_simiple_main);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            {
                BuildInformationFragment fragment = new BuildInformationFragment();
                transaction.add(R.id.Content_List_Root, fragment, fragment.createSimpleTag());
            }
            transaction.commit();
        }
    }

    public static InformationFragmentMain createInstance(Context context) {
        return new InformationFragmentMain();
    }
}
