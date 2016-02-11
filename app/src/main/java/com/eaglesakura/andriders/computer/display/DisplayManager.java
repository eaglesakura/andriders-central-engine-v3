package com.eaglesakura.andriders.computer.display;

import com.eaglesakura.andriders.computer.CycleComputerManager;
import com.eaglesakura.andriders.computer.display.computer.DisplayDataImpl;
import com.eaglesakura.andriders.computer.extension.client.ExtensionClient;
import com.eaglesakura.andriders.extension.DisplayInformation;
import com.eaglesakura.andriders.extension.display.DisplayData;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.util.Util;

import android.content.Context;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * サイコンの表示内容を管理する
 *
 * 処理自体はコンストラクタで指定されたパイプラインに投げ込まれるため、非同期かつ直列的に行われる。
 */
public class DisplayManager extends CycleComputerManager {

    /**
     * 表示内容一覧
     */
    Map<String, DisplayDataImpl> values = new HashMap<>();

    public DisplayManager(Context context) {
        super(context);
    }

    /**
     * 値を保持させる
     */
    public void putValue(final ExtensionClient extension, final DisplayDataImpl value) {
        putValue(extension, Arrays.asList(value));
    }

    /**
     * 値を一括で登録する
     */
    public void putValue(final ExtensionClient extension, final List<DisplayDataImpl> impl) {
        if (Util.isEmpty(impl)) {
            return;
        }

        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                synchronized (values) {
                    for (DisplayDataImpl value : impl) {
                        values.put(createKey(extension, value), value);
                    }
                }
            }
        });
    }

    /**
     * Viewに対して保持されている値をバインドさせる
     * 処理はパイプラインに行われて非同期にチェックされ、View処理はUIスレッドで行われる。
     */
    public void bindUI(final ExtensionClient extension, final DisplayInformation info, final ViewGroup slotStub) {
        AndroidThreadUtil.assertUIThread();

        DisplayDataImpl impl;
        synchronized (values) {
            impl = values.get(createKey(extension, info));
        }
        if (impl == null) {
            DisplayDataImpl.bindNotAvailable(getContext(), slotStub);
        } else {
            impl.bindView(getContext(), slotStub);
        }
    }

    /**
     * 定時更新を行わせる
     */
    @Override
    public void updateInPipeline(final double deltaTimeSec) {
    }

    /**
     * 管理用のキーに変換する
     */
    private String createKey(ExtensionClient extension, DisplayData displayValue) {
        return String.format("%s@%s", extension.getInformation().getId(), displayValue.getId());
    }

    /**
     * 管理用のキーに変換する
     */
    private String createKey(ExtensionClient extension, DisplayInformation info) {
        return String.format("%s@%s", extension.getInformation().getId(), info.getId());
    }
}
